package uk.al_richard.branch_git_experiment;

import java.util.logging.Logger;

import eu.similarity.msc.data.ExperimentalData;
import eu.similarity.msc.local_context.LocalContext;

public class MyLocalContext extends LocalContext {

	@Override
	public String getUsername() {
		return "Richard";
	}

	@Override
	public String getLocalFileRoot() {
		return "/Volumes/Data/MetricSpaceContextData";
	}

}
