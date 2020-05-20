package com.blocklang.core.runner.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Runner {

	private static final String EXPR_PATTERN = "\\$\\{\\{([a-zA-Z0-9|\\.|_]+)\\}\\}";
	private Pattern pattern = Pattern.compile(EXPR_PATTERN);
	
	public void run(Workflow workflow) {
		List<Job> jobs = workflow.getJobs();
		for(var job : jobs) {
			runJob(job);
		}
	}

	private void runJob(Job job) {
		List<Step> steps = job.getSteps();
		
		// 将表达式转换为值，因为这些需要使用 steps 上下文，所以将代码放在此处
		for(var step: steps) {
			List<StepWith> with = step.getWith();
			for(StepWith withItem : with) {
				Object value = evaluate(steps, withItem);
				withItem.setValue(value);
			}
		}
		
		// 运行
		for(var step : steps) {
			runStep(step);
		}
	}

	private String evaluate(List<Step> steps, StepWith withItem) {
		String value = withItem.getValue().toString();
		
		List<String> exprs = stripExpression(withItem);
		for(String expr : exprs) {
			String[] segments = expr.split("\\.");
			// 如果是在 steps 上下文查找
			// steps.{stepId}.outputs.{outputName}
			if("steps".equals(segments[0])) {
				String stepId = segments[1];
				String outputName = segments[3];
				Step matchedStep = findStep(steps, stepId);
				if(matchedStep != null) {
					Object output = matchedStep.getUses().getOutput(outputName);
					value = value.replaceAll("\\$\\{\\{" + expr + "\\}\\}", output.toString());
				}
			}
		}
		return value;
	}

	private Step findStep(List<Step> steps, String stepId) {
		for(var step1 : steps) {
			if(stepId.equals(step1.getId())) {
				return step1;
			}
		}
		return null;
	}

	// 提取出表达式，先实现一个简单版
	private List<String> stripExpression(StepWith withItem) {
		Matcher matcher = pattern.matcher(withItem.getExpression());
		List<String> exprs = new ArrayList<String>();
		while(matcher.find()) {
			exprs.add(matcher.group(1));
		}
		return exprs;
	}

	private void runStep(Step step) {
		List<StepWith> inputs = step.getWith();
		var action = step.getUses();
		action.setInputs(inputs);
		action.run();
	}

}
