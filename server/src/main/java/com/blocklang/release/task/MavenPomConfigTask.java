package com.blocklang.release.task;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.nimbusds.oauth2.sdk.util.StringUtils;

public class MavenPomConfigTask extends AbstractTask{

	public MavenPomConfigTask(AppBuildContext appBuildContext) {
		super(appBuildContext);
	}

	@Override
	public Optional<Boolean> run() {
		SAXReader reader = new SAXReader();
		// 如果不添加命名空间，则 xpath 总是返回 null
		Map<String, String> namespaceURIs = new HashMap<String, String>();
		namespaceURIs.put("m", "http://maven.apache.org/POM/4.0.0");
		reader.getDocumentFactory().setXPathNamespaceURIs(namespaceURIs);
		try {
			Document document = reader.read(appBuildContext.getMavenPomFile().toFile());
			
			Node groupIdNode = document.selectSingleNode("/project/m:groupId");
			// com.blocklang.{owner}
			groupIdNode.setText(groupIdNode.getText().trim() + "." + appBuildContext.getOwner());
			
			Node artifactIdNode = document.selectSingleNode("/project/m:artifactId");
			// {project}
			artifactIdNode.setText(appBuildContext.getRepoName());
			
			Node nameNode = document.selectSingleNode("/project/m:name");
			// {project}
			nameNode.setText(appBuildContext.getRepoName());
			
			Node versionNode = document.selectSingleNode("/project/m:version");
			versionNode.setText(appBuildContext.getVersion());
			
			// description 不是必填项
			if(StringUtils.isNotBlank(appBuildContext.getDescription())) {
				Node descriptionNode = document.selectSingleNode("/project/m:description");
				descriptionNode.setText(appBuildContext.getDescription());
			}
			
			Node jdkVersionNode = document.selectSingleNode("/project/m:properties/m:java.version");
			jdkVersionNode.setText(String.valueOf(appBuildContext.getJdkMajorVersion()));
			
			try(FileWriter out = new FileWriter(appBuildContext.getMavenPomFile().toFile())){
				document.write(out);
			}
			
			return Optional.of(true);
		} catch (DocumentException | IOException | IllegalArgumentException e) {
			appBuildContext.error(e);
		}
		return Optional.empty();
	}

}
