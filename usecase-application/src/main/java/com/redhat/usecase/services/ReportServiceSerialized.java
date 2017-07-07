package com.redhat.usecase.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.marshalling.MarshallerFactory;

import com.redhat.usecase.TrackingAgendaEventListener;
import com.redhat.usecase.util.BRMSUtil;

import report.Report;
import tradeEvent.TradeEvent;

public class ReportServiceSerialized {
	private static ArrayList<Report> reports;
	private static BRMSUtil brmsUtil;

	public ReportServiceSerialized() {

		reports = new ArrayList<Report>();

	}

	public static void addToReports(Report report) {
		reports.add(report);
	}

	public ArrayList<Report> fireAllRules(ArrayList<TradeEvent> inputTrades)
			throws ClassNotFoundException, IOException {
		TrackingAgendaEventListener listener = new TrackingAgendaEventListener();
		createSession();
		KieSession session = load();

		for (TradeEvent trade : inputTrades) {
			List<Command> list = new ArrayList<Command>();
			System.out.println("TRADE: " + trade);
			// session.addEventListener(listener);

			list.add(CommandFactory.newInsert(trade));
			session.insert(trade);

		}
		write(session);

		session.fireAllRules();

		QueryResults queryResults = session.getQueryResults("reports generated");

		for (QueryResultsRow row : queryResults) {
			Report report = (Report) row.get("$report");
			addToReports(report);
		}

		// uncomment this to print out rules that fired
		// System.out.println(listener.getMatchList());
		return reports;
	}

	private void createSession() throws IOException {
		brmsUtil = new BRMSUtil("serialize-session-example", "stream");
		KieSession session = brmsUtil.getStatefulSession("realtime");
		write(session);
	}

	private void write(KieSession session) throws IOException {
		FileOutputStream fos = new FileOutputStream("src/main/java/com/redhat/usecase/services/file.xml");
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		KieSessionConfiguration kieSessionConfiguration = session.getSessionConfiguration();
		oos.writeObject(kieSessionConfiguration);

		Marshaller marshaller = createSerializableMarshaller(brmsUtil.getKieBase());
		marshaller.marshall(fos, session);

	}

	private KieSession load() throws ClassNotFoundException, IOException {
		FileInputStream fis = new FileInputStream("src/main/java/com/redhat/usecase/services/file.xml");
		ObjectInputStream ois = new ObjectInputStream(fis);

		KieSessionConfiguration kieSessionConfiguration = (KieSessionConfiguration) ois.readObject();

		Marshaller marshaller = createSerializableMarshaller(brmsUtil.getKieBase());
		return marshaller.unmarshall(fis, kieSessionConfiguration, null);

	}

	private Marshaller createSerializableMarshaller(KieBase kBase) {
		ObjectMarshallingStrategyAcceptor acceptor = MarshallerFactory.newClassFilterAcceptor(new String[] { "*.*" });
		ObjectMarshallingStrategy strategy = MarshallerFactory.newSerializeMarshallingStrategy(acceptor);
		Marshaller marshaller = MarshallerFactory.newMarshaller(kBase, new ObjectMarshallingStrategy[] { strategy });
		return marshaller;
	}

}
