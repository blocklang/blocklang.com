package com.blocklang.marketplace.apirepo;

import java.util.List;

import com.blocklang.core.git.GitBlobInfo;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 将 Widget、Service 和 WebApi 等统称为 ApiObject
 * 
 * 如果有一个 ApiObject 解析出错，则只停止此 ApiObject 的解析，然后继续解析后续的 ApiObject
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiObjectParser {

	private ApiRepoPathReader pathReader = new ApiRepoPathReader();
	private ChangelogFileParser changelogFileParser;
	
	public ApiObjectParser(ChangeParserFactory changeParserFactory) {
		this.changelogFileParser = new ChangelogFileParser(changeParserFactory);
	}

	public boolean run(ChangedObjectContext changedObjectContext, 
			List<GitBlobInfo> changelogFiles) {

		boolean parserApiObjectSuccess = true;
		for (GitBlobInfo jsonFile : changelogFiles) {
			// 如果 changelog 文件已执行过，则忽略
			String fileId = pathReader.read(jsonFile.getName()).getOrder();
			if(changedObjectContext.changelogFileParsed(fileId)) {
				break;
			}

			if(!changelogFileParser.run(changedObjectContext, jsonFile)) {
				parserApiObjectSuccess = false;
				break;
			}

			PublishedFileInfo parsedFile = new PublishedFileInfo();
			parsedFile.setFileId(fileId);
			parsedFile.setMd5sum(DigestUtils.md5Hex(jsonFile.getContent()));
			parsedFile.setVersion(changedObjectContext.getShortRefName());
			
			changedObjectContext.addParsedChangelogFile(parsedFile);
		}

		return parserApiObjectSuccess;
	}

}
