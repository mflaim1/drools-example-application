package com.redhat.usecase.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;

import com.redhat.usecase.TrackingAgendaEventListener;

import report.Report;
import tradeEvent.TradeEvent;

public class ReportServicePartitioned {
	private static ArrayList<Report> reports;

	public ReportServicePartitioned() {

		reports = new ArrayList<Report>();

	}

	public static void addToReports(Report report) {
		reports.add(report);
	}

	public ArrayList<Report> fireAllRules(ArrayList<TradeEvent> inputTrades) {

		SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
		JavaSparkContext sc = new JavaSparkContext(conf);

		JavaRDD<TradeEvent> trades = sc.parallelize(inputTrades);

		// group by financial advisor and apply the corresponding rules
		JavaPairRDD<String, Iterable<TradeEvent>> tradesByFA = trades.groupBy(t -> t.getFinancialAdvisorId());

		System.out.println("NEW KIEBASE");
		KieBase rulesFA = loadRules("suspicious-fas");
		Broadcast<KieBase> broadcastKieBase1 = sc.broadcast(rulesFA);
		long numberOfGroups = tradesByFA.mapValues(f -> applyRulesInKieBase(f, broadcastKieBase1.value())).count();
		System.out.println("NUMBER OF SESSIONS FINISHED: " + numberOfGroups);

		// group by account and apply the corresponding rules
		JavaPairRDD<String, Iterable<TradeEvent>> tradesByAccounts = trades.groupBy(t -> t.getAccountId());

		System.out.println("NEW KIEBASE");
		KieBase rulesAccount = loadRules("suspicious-accounts");
		Broadcast<KieBase> broadcastKieBase2 = sc.broadcast(rulesAccount);
		numberOfGroups = tradesByAccounts.mapValues(f -> applyRulesInKieBase(f, broadcastKieBase2.value())).count();
		System.out.println("NUMBER OF SESSIONS FINISHED: " + numberOfGroups);

		sc.stop();
		sc.close();
		return reports;
	}

	public static KieBase loadRules(String kieBaseName) {

		KieServices kServices = KieServices.Factory.get();

		ReleaseId releaseId = kServices.newReleaseId("com.redhat", "partition-trades-example", "LATEST");

		KieContainer kContainer = kServices.newKieContainer(releaseId);

		KieScanner kScanner = kServices.newKieScanner(kContainer);

		return kContainer.getKieBase(kieBaseName);
	}

	public static Iterable<TradeEvent> applyRulesInKieBase(Iterable<TradeEvent> trades, KieBase rules) {

		TrackingAgendaEventListener listener = new TrackingAgendaEventListener();

		// loops through each group of trades, creates session, and fires rules

		System.out.println("NEW SESSION");
		StatelessKieSession session = rules.newStatelessKieSession();

		// uncomment this to add the event listener
		// session.addEventListener(listener);

		Iterator<TradeEvent> iterator = trades.iterator();

		List<Command> list = new ArrayList<Command>();

		// insert rules into session
		while (iterator.hasNext()) {
			TradeEvent nextTrade = iterator.next();
			System.out.println("TRADE: " + nextTrade.toString());

			list.add(CommandFactory.newInsert(nextTrade));
		}

		list.add(CommandFactory.newFireAllRules());
		list.add(CommandFactory.newQuery("reports generated", "reports generated"));

		ExecutionResults results = session.execute(CommandFactory.newBatchExecution(list));
		QueryResults queryResults = (QueryResults) results.getValue("reports generated");

		for (QueryResultsRow row : queryResults) {
			Report report = (Report) row.get("$report");
			addToReports(report);
		}

		// uncomment this to print out rules that fired
		// System.out.println(listener.getMatchList());
		return trades;

	}

}
