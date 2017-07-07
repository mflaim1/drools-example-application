package com.redhat.usecase.services;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.drools.core.time.SessionPseudoClock;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

import com.redhat.usecase.TrackingAgendaEventListener;
import com.redhat.usecase.util.BRMSUtil;

import report.Report;
import tradeEvent.TradeEvent;

public class ReportService {

	public ArrayList<Report> reports = new ArrayList<Report>();

	public ArrayList<TradeEvent> trades = new ArrayList<TradeEvent>();

	public ReportService() {

	}

	public void fireAllRules(String artifactId, ArrayList<TradeEvent> inputTrades, String mode, String clockType) {

		TrackingAgendaEventListener listener = new TrackingAgendaEventListener();

		BRMSUtil brmsUtil = new BRMSUtil(artifactId, mode);
		KieSession ksession = brmsUtil.getStatefulSession(clockType);

		ksession.addEventListener(listener);

		for (TradeEvent trade : inputTrades) {

			ksession.insert(trade);
			if (clockType.equals("pseudo")) {
				SessionPseudoClock clock = ksession.getSessionClock();
				clock.advanceTime(6, TimeUnit.MINUTES);
			}
		}
		try {
			ksession.fireAllRules();
		} finally {
			QueryResults queryReportsResults = (QueryResults) ksession.getQueryResults("reports generated");
			QueryResults queryTradesResults = (QueryResults) ksession.getQueryResults("trades");

			// System.out.println(listener.getMatchList());

			for (QueryResultsRow row : queryReportsResults) {
				Report report = (Report) row.get("$report");
				reports.add(report);
			}

			for (QueryResultsRow row : queryTradesResults) {
				TradeEvent trade = (TradeEvent) row.get("$trade");
				trades.add(trade);
			}

			if (ksession != null) {

				ksession.dispose();
			}
		}
	}
}
