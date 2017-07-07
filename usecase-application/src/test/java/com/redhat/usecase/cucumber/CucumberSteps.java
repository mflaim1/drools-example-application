package com.redhat.usecase.cucumber;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.junit.Assert;

import com.redhat.usecase.services.ReportService;
import com.redhat.usecase.services.ReportServicePartitioned;
import com.redhat.usecase.services.ReportServiceSerialized;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import report.Report;
import stockPriceEvent.StockPriceEvent;
import tradeEvent.TradeEvent;

public class CucumberSteps {

	ArrayList<TradeEvent> trades = new ArrayList<TradeEvent>();
	ArrayList<StockPriceEvent> prices = new ArrayList<StockPriceEvent>();

	ReportService reportService = new ReportService();

	ReportServicePartitioned reportServicePartitioned = new ReportServicePartitioned();

	ReportServiceSerialized reportServiceSerialized = new ReportServiceSerialized();

	ArrayList<Report> reportsGenerated = new ArrayList<Report>();
	ArrayList<TradeEvent> tradesInMemory = new ArrayList<TradeEvent>();

	@Given("^Trade Events:$")
	public void trade_events(DataTable table) throws Throwable {

		for (Map<String, String> row : table.asMaps(String.class, String.class)) {
			TradeEvent trade = new TradeEvent();

			trade.setId(row.get("Id"));
			trade.setAccountId(row.get("Investor Id"));
			trade.setFinancialAdvisorId(row.get("Advisor Id"));
			trade.setPrice(Double.parseDouble(row.get("Price")));
			trade.setQuantity(Integer.parseInt(row.get("Quantity")));
			trade.setSymbol(row.get("Symbol"));
			trade.setType(row.get("Type"));

			if (row.get("Timestamp") != null) {

				trade.setTimestamp(parseDate(row.get("Timestamp")));

			}

			trades.add(trade);

		}
	}

	@Given("^a Market Price Change Event:$")
	public void a_market_price_change_event(DataTable table) throws Throwable {
		for (Map<String, String> row : table.asMaps(String.class, String.class)) {
			StockPriceEvent stockPrice = new StockPriceEvent();
			stockPrice.setPrice(Double.parseDouble(row.get("Price")));
			stockPrice.setSymbol(row.get("Symbol"));
			stockPrice.setTimestamp(parseDate(row.get("Timestamp")));

			prices.add(stockPrice);
		}
	}

	@When("^I run the cloud mode example$")
	public void i_run_the_cloud_mode_example_rules() throws Throwable {
		// Map a business name to the artifactId
		reportService.fireAllRules("event-processing-mode-example", trades, "cloud", "realtime");
		reportsGenerated = reportService.reports;

	}

	@When("^I run the stream mode example$")
	public void i_run_the_stream_mode_example_rules() throws Throwable {
		// Map a business name to the artifactId
		reportService.fireAllRules("event-processing-mode-example", trades, "stream", "realtime");
		reportsGenerated = reportService.reports;

	}

	@When("^the \"([^\"]*)\" rules are executed$")
	public void the_rule_is_executed(String artifactId) throws Throwable {
		// Map a business name to the artifactId

		reportService.fireAllRules(artifactId, trades, "cloud", "realtime");
		reportsGenerated = reportService.reports;

	}

	@When("^I run the partition example$")
	public void i_run_the_parition_example_rules() throws Throwable {
		reportsGenerated = reportServicePartitioned.fireAllRules(trades);
	}

	@When("^I run the realtime clock example$")
	public void i_run_the_realtime_clock_example_rules() throws Throwable {
		reportService.fireAllRules("clocks-example", trades, "stream", "realtime");
		reportsGenerated = reportService.reports;
	}

	@When("^I run the pseudo clock example$")
	public void i_run_the_pseudo_clock_example_rules() throws Throwable {
		reportService.fireAllRules("clocks-example", trades, "stream", "pseudo");
		reportsGenerated = reportService.reports;

	}

	@When("^I run the serialization example$")
	public void i_run_the_serialization_example() throws Throwable {
		// Write code here that turns the phrase above into concrete actions
		reportsGenerated = reportServiceSerialized.fireAllRules(trades);
	}

	@Then("^I expect the following Reports to be created:$")
	public void i_expect_the_following_Report_to_be_created(DataTable table) throws Throwable {
		ArrayList<Report> reports = new ArrayList<Report>();
		for (Map<String, String> row : table.asMaps(String.class, String.class)) {
			Report report = new Report();
			report.setCode(row.get("Code"));

			ArrayList<String> list = new ArrayList<String>(Arrays.asList(row.get("Trades").split(",")));
			report.setTrades(list);
			reports.add(report);
		}

		Assert.assertTrue(arraysMatch(reports, reportsGenerated));

	}

	@Then("^I expect no reports to be created$")
	public void i_expect_no_reports_to_be_created() throws Throwable {
		System.out.println("REPORTS GENERATED" + reportsGenerated);
		Assert.assertTrue(reportsGenerated.isEmpty() || reportsGenerated == null);

	}

	@When("^I run the explicit expiry example$")
	public void i_run_the_explicit_expiry_example() throws Throwable {
		reportService.fireAllRules("memory-management-explicit-expiry", trades, "stream", "pseudo");
		reportsGenerated = reportService.reports;
		tradesInMemory = reportService.trades;
	}

	@Then("^I expect no Trades to exist:$")
	public void i_expect_no_Trades_to_exist(DataTable arg1) throws Throwable {
		System.out.println("TRADES EXISTING" + tradesInMemory);
		Assert.assertTrue(tradesInMemory.isEmpty() || tradesInMemory == null);
	}

	@When("^I run the explicit removal example$")
	public void i_run_the_explicit_removal_example() throws Throwable {
		reportService.fireAllRules("memory-management-explicit-removal", trades, "stream", "realtime");
		reportsGenerated = reportService.reports;
		tradesInMemory = reportService.trades;
	}

	@When("^I run the inferred expiration example$")
	public void i_run_the_inferred_expiration_example() throws Throwable {
		reportService.fireAllRules("memory-management-usecase-example", trades, "stream", "pseudo");
		reportsGenerated = reportService.reports;
		tradesInMemory = reportService.trades;
	}

	public Date parseDate(String stringDate) throws ParseException {
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date result = df.parse(stringDate);
		return result;
	}

	public boolean arraysMatch(ArrayList<Report> expected, ArrayList<Report> generated) {
		System.out.println("REPORTS GENERATED" + generated.toString());
		System.out.println("REPORTS EXPECTED" + expected.toString());
		if (expected.size() != generated.size()) {
			return false;
		}
		for (Report reportExpected : expected) {
			boolean match = false;
			for (Report reportGenerated : generated) {
				if (reportExpected.getCode().equals(reportGenerated.getCode())
						&& reportExpected.getTrades().containsAll(reportGenerated.getTrades())
						&& reportGenerated.getTrades().containsAll(reportExpected.getTrades())) {
					match = true;
				}
			}
			if (match == false) {
				return false;
			}
		}

		return true;
	}

}