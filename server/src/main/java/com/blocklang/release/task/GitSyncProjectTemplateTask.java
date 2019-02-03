package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.Optional;

import com.blocklang.core.git.GitUtils;
import com.blocklang.core.git.exception.GitCloneFailedException;
import com.blocklang.core.git.exception.GitPullFailedException;
import com.blocklang.core.git.exception.GitRepoNotFoundException;

public class GitSyncProjectTemplateTask extends AbstractTask{

	public GitSyncProjectTemplateTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	@Override
	public Optional<Boolean> run() {
		try {
			Path path = appBuildContext.getProjectTemplateDirectory();
			if(GitUtils.isGitRepo(path)) {
				GitUtils.pull(path);
			} else {
				GitUtils.clone(appBuildContext.getTemplateProjectGitUrl(), path);
			}
			return Optional.of(true);
		} catch (GitRepoNotFoundException |GitPullFailedException | GitCloneFailedException e) {
			appBuildContext.error(e);
			return Optional.empty();
		}
	}
	
}
