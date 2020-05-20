package com.blocklang.core.runner.common;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;

public class RunnerTest {

	@Test
	public void run_success() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action = mock(AbstractAction.class);
				step1.setUses(action);
			job1.addStep(step1);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		runner.run(workflow);
		
		verify(action).run();
		verify(action).setInputs(eq(Collections.emptyList()));
	}
	
	@Test
	public void run_set_inputs_for_action() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action = mock(AbstractAction.class);
				step1.setUses(action);
				step1.addWith("input1", "value1"); // 将 with 中的数据传给 action
			job1.addStep(step1);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		runner.run(workflow);
		
		verify(action).run();
		
		verify(action).setInputs(argThat((list) -> {
			StepWith withItem = list.get(0);
			return withItem.getKey().equals("input1")
					&& withItem.getExpression().equals("value1")
					&& withItem.getValue().equals("value1");
		}));
	}
	
	@Test
	public void run_two_action() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action1 = mock(AbstractAction.class);
				step1.setUses(action1);
				job1.addStep(step1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				step2.setUses(action2);
				job1.addStep(step2);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		runner.run(workflow);
		
		verify(action1).run();
		verify(action2).run();
	}
	
	@Test
	public void run_get_outputs_one_output_for_action() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action1 = mock(AbstractAction.class);
				when(action1.getOutput(eq("output_key1"))).thenReturn("world");
				step1.setUses(action1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				step2.setUses(action2);
				job1.addStep(step2);
				// 传入表达式，引用 action1 的 output
				step2.addWith("input2", "Hello ${{steps.step_1.outputs.output_key1}}");
			job1.addStep(step1);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		runner.run(workflow);
		
		verify(action1).run();
		verify(action2).run();
		
		verify(action2).setInputs(argThat((list) -> {
			StepWith withItem = list.get(0);
			return withItem.getKey().equals("input2")
					&& withItem.getExpression().equals("Hello ${{steps.step_1.outputs.output_key1}}")
					&& withItem.getValue().equals("Hello world");
		}));
	}
	

	@Test
	public void run_get_outputs_two_output_for_action() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action1 = mock(AbstractAction.class);
				when(action1.getOutput(eq("output_key1"))).thenReturn("b");
				when(action1.getOutput("output_key2")).thenReturn("c");
				step1.setUses(action1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				step2.setUses(action2);
				job1.addStep(step2);
				// 传入表达式，引用 action1 的 output
				step2.addWith("input2", "a ${{steps.step_1.outputs.output_key1}} ${{steps.step_1.outputs.output_key2}}");
			job1.addStep(step1);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		runner.run(workflow);
		
		verify(action1).run();
		verify(action2).run();
		
		verify(action2).setInputs(argThat((list) -> {
			StepWith withItem = list.get(0);
			return withItem.getKey().equals("input2")
					&& withItem.getExpression().equals("a ${{steps.step_1.outputs.output_key1}} ${{steps.step_1.outputs.output_key2}}")
					&& withItem.getValue().equals("a b c");
		}));
	}

}
