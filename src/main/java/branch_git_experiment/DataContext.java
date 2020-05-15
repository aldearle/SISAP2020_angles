package branch_git_experiment;

import eu.similarity.msc.local_context.LocalContext;

public class DataContext extends LocalContext {

	@Override
	public String getUsername() {
		return "Richard";
	}

	@Override
	public String getLocalFileRoot() {
		return "/Volumes/Data/MetricSpaceContextData";
	}

}
