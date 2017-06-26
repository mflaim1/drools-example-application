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

	public ReportService() {

	}

	// pass facts and artifact id in via a parameter
	public ArrayList<Report> fireAllRules(String artifactId, ArrayList<TradeEvent> trades, String mode,
			String clockType) {

		ArrayList<Report> reports = new ArrayList<Report>();
		TrackingAgendaEventListener listener = new TrackingAgendaEventListener();

		BRMSUtil brmsUtil = new BRMSUtil(artifactId, mode);
		KieSession ksession = brmsUtil.getStatefulSession(clockType);

		// ksession.addEventListener(listener);

		for (TradeEvent trade : trades) {

			ksession.insert(trade);
			if (clockType.equals("pseudo")) {
				SessionPseudoClock clock = ksession.getSessionClock();
				clock.advanceTime(6, TimeUnit.MINUTES);
			}
		}
		try {
			ksession.fireAllRules();
		} finally {
			QueryResults queryResults = (QueryResults) ksession.getQueryResults("reports generated");

			// System.out.println(listener.getMatchList());

			for (QueryResultsRow row : queryResults) {
				Report report = (Report) row.get("$report");
				reports.add(report);
			}
			if (ksession != null) {

				ksession.dispose();
			}
		}
		return reports;
	}
}
