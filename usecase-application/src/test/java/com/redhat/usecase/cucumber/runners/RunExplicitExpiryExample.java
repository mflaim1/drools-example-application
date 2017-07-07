package com.redhat.usecase.cucumber.runners;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/java/com/redhat/usecase/cucumber/features/memory_management_example/MemoryManagementExplicitExpiry.feature" }, glue = {
		"com.redhat.usecase.cucumber" })
public class RunExplicitExpiryExample {
}
