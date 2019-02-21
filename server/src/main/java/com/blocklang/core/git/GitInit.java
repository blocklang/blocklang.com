package com.blocklang.core.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;

import com.blocklang.core.git.exception.FileCreateOrUpdateFailedException;
import com.blocklang.core.git.exception.GitInitFailedException;
import com.blocklang.core.git.exception.GitRepoNotFoundException;

public class GitInit {

	private static final String MSG_FIRST_COMMIT = "First Commit";
	
	private Path gitRepoPath;
	private String gitUserName;
	private String gitUserMail;
	
	private Map<String, String> files = new HashMap<String, String>();
	
	public GitInit() {
		
	}
	
	public GitInit(Path gitRepoPath, String gitUserName, String gitUserMail) {
		this.gitRepoPath = gitRepoPath;
		this.gitUserName = gitUserName;
		this.gitUserMail = gitUserMail;
	}
	
	public GitInit addFile(String fileName, String fileContent) {
		files.put(fileName, fileContent);
		return this;
	}
	
	public String commit(String commitMessage) {
		
		InitCommand command = new InitCommand();
		File directory = this.gitRepoPath.toFile();
		command.setDirectory(directory);
		
		try (Repository repository = command.call().getRepository();
				Git git = new Git(repository)){
			// 配置仓库
			config(git, this.gitUserName, this.gitUserMail);
			
			AddCommand addCommand = git.add();
			files.forEach((key, value) -> {
				newFile(this.gitRepoPath, key, value);
				addCommand.addFilepattern(key);
			});
			addCommand.call();
			RevCommit commit = git.commit().setMessage(commitMessage).call();
			return commit.getName();
		} catch (GitAPIException e) {
			throw new GitInitFailedException(e);
		} catch (IOException e) {
			throw new GitRepoNotFoundException(gitRepoPath.toString());
		}
		
	}
	
	// 在 git 仓库根目录下创建文件。
	private void newFile(Path folder, String fileName, String fileContent){
		try{
			Path file = folder.resolve(fileName);
			if(Files.notExists(file)){
				Files.createFile(file);
			}
			
			Files.writeString(file, fileContent);
		}catch(IOException e){
			throw new FileCreateOrUpdateFailedException(e);
		}
	}

	/**
	 * 初始化git仓库
	 * 
	 * @param gitRepoPath 仓库路径，绝对路径
	 * @param gitUserName 用户名
	 * @param gitUserMail 用户邮箱
	 * @return 第一次提交的 id
	 */
	public String execute(Path gitRepoPath, String gitUserName, String gitUserMail){
		InitCommand command = new InitCommand();
		File directory = gitRepoPath.toFile();
		command.setDirectory(directory);
		
		try (Repository repository = command.call().getRepository();
				Git git = new Git(repository)){
			// 配置仓库
			config(git, gitUserName, gitUserMail);
			RevCommit commit = git.commit().setMessage(MSG_FIRST_COMMIT).call();
			return commit.getName();
		} catch (GitAPIException e) {
			throw new GitInitFailedException(e);
		} catch (IOException e) {
			throw new GitRepoNotFoundException(gitRepoPath.toString());
		}
	}
	
	private void config(Git git, String gitUserName, String gitUserMail) throws IOException{
		StoredConfig config = git.getRepository().getConfig();
		if(StringUtils.isNotBlank(gitUserName)){
			config.setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME, gitUserName);
		}
		if(StringUtils.isNotBlank(gitUserMail)){
			config.setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL, gitUserMail);
		}
		config.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_FILEMODE, false);
		config.save();
	}

}
