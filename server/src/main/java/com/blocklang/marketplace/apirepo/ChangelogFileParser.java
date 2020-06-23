package com.blocklang.marketplace.apirepo;

import java.util.ArrayList;
import java.util.List;

import com.blocklang.core.git.GitBlobInfo;
import com.blocklang.core.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class ChangelogFileParser {

	private ChangeParserFactory changeParserFactory;

	public ChangelogFileParser(ChangeParserFactory factory) {
		this.changeParserFactory = factory;
	}

	public boolean run(ApiObjectContext context, GitBlobInfo jsonFile) {
		List<Change> changeParses = readChanges(jsonFile);
		return applyChanges(context, changeParses);
	}
	
	private List<Change> readChanges(GitBlobInfo jsonFile) {
		List<Change> changeParses = new ArrayList<>();
		try {
			JsonNode jsonNode = JsonUtil.readTree(jsonFile.getContent());
			JsonNode changeNodes = jsonNode.get("changes");

			for (JsonNode changeNode : changeNodes) {
				Change changeParser = changeParserFactory.create(changeNode);
				if(changeParser == null) {
					break;
				}
				changeParses.add(changeParser);
			}
		} catch (JsonProcessingException e) {
			// do nothing
		}
		return changeParses;
	}

	private boolean applyChanges(ApiObjectContext context, List<Change> changeParses) {
		boolean allOperatorValid = true;
		for(Change parser : changeParses) {
			if(!parser.apply(context)) {
				allOperatorValid = false;
			}
		}
		return allOperatorValid;
	}
}
