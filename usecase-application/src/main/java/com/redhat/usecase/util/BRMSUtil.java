package com.redhat.usecase.util;

import javax.inject.Singleton;

import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;

@Singleton
public class BRMSUtil {

	private KieContainer kContainer = null;
	private KieBase kBase = null;

	public BRMSUtil(String artifactId, String mode) {

		KieServices kServices = KieServices.Factory.get();

		ReleaseId releaseId = kServices.newReleaseId("com.redhat", artifactId, "LATEST");
		kContainer = kServices.newKieContainer(releaseId);

		// configure process mode type
		KieBaseConfiguration kBaseConfig = KieServices.Factory.get().newKieBaseConfiguration();
		if (mode.equals("stream")) {
			kBaseConfig.setOption(EventProcessingOption.STREAM);
		} else {
			kBaseConfig.setOption(EventProcessingOption.CLOUD);
		}

		kBase = kContainer.newKieBase(kBaseConfig);

		KieScanner kScanner = kServices.newKieScanner(kContainer);

	}

	public KieBase getKieBase() {
		return kBase;
	}

	public StatelessKieSession getStatelessSession() {

		return kBase.newStatelessKieSession();

	}

	/*
	 * KieSession is the new StatefulKnowledgeSession from BRMS 5.3.
	 */
	public KieSession getStatefulSession(String clockType) {
		KieSessionConfiguration kSessionConfig = KieServices.Factory.get().newKieSessionConfiguration();
		kSessionConfig.setOption(ClockTypeOption.get(clockType));
		return kBase.newKieSession(kSessionConfig, null);
	}
}