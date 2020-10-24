package com.blocklang.release.data;

abstract class ProjectStore {

	private String dataRootPath;
	private String owner;
	private String repositoryName;
	private String projectName;

	protected ProjectStore(String dataRootPath, String owner, String repositoryName, String projectName) {
		this.dataRootPath = dataRootPath;
		this.owner = owner;
		this.repositoryName = repositoryName;
		this.projectName = projectName;
	}

	public String getDataRootPath() {
		return dataRootPath;
	}

	public String getOwner() {
		return owner;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public String getProjectName() {
		return projectName;
	}

}
