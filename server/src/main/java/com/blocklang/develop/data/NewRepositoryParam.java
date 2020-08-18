package com.blocklang.develop.data;

public class NewRepositoryParam extends CheckRepositoryNameParam{
	
	private String description;
	private Boolean isPublic;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}
	
}
