package com.blocklang.release.task;

import java.nio.file.Path;
import java.util.Optional;

import com.blocklang.git.GitUtils;
import com.blocklang.git.exception.GitCloneFailedException;
import com.blocklang.git.exception.GitPullFailedException;
import com.blocklang.git.exception.GitRepoNotFoundException;

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
				GitUtils.clone(appBuildContext.getProjectTemplateGitUrl(), path);
			}
			return Optional.of(true);
		} catch (GitRepoNotFoundException |GitPullFailedException | GitCloneFailedException e) {
			appBuildContext.error(e);
			return Optional.empty();
		}
	}
	
}
