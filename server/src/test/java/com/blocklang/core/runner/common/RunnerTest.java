package com.blocklang.core.runner.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;

public class RunnerTest {

	@Test
	public void run_one_action_success() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action = mock(AbstractAction.class);
				when(action.run()).thenReturn(true);
				step1.setUses(action);
			job1.addStep(step1);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		assertThat(runner.run(workflow)).isTrue();

		verify(action).run();
		verify(action).setInputs(eq(Collections.emptyList()));
	}
	
	@Test
	public void run_one_action_failed() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action = mock(AbstractAction.class);
				when(action.run()).thenReturn(false);
				step1.setUses(action);
			job1.addStep(step1);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		assertThat(runner.run(workflow)).isFalse();

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
	public void run_two_action_success() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action1 = mock(AbstractAction.class);
				when(action1.run()).thenReturn(true);
				step1.setUses(action1);
				job1.addStep(step1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				when(action2.run()).thenReturn(true);
				step2.setUses(action2);
				job1.addStep(step2);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		assertThat(runner.run(workflow)).isTrue();
		
		verify(action1).run();
		verify(action2).run();
	}
	
	@Test
	public void run_two_action_but_first_action_failed() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action1 = mock(AbstractAction.class);
				when(action1.run()).thenReturn(false);
				step1.setUses(action1);
				job1.addStep(step1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				when(action2.run()).thenReturn(true);
				step2.setUses(action2);
				job1.addStep(step2);
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		assertThat(runner.run(workflow)).isFalse();
		
		verify(action1).run();
		verify(action2, never()).run();
	}
	
	@Test
	public void run_get_outputs_one_output_for_action() {
		Workflow workflow = new Workflow("I_am_a_workflow");
			Job job1 = new Job("job_1");
				Step step1 = new Step("step_1");
				AbstractAction action1 = mock(AbstractAction.class);
				when(action1.run()).thenReturn(true);
				when(action1.getOutput(eq("output_key1"))).thenReturn("world");
				step1.setUses(action1);
				job1.addStep(step1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				when(action2.run()).thenReturn(true);
				step2.setUses(action2);
				// 传入表达式，引用 action1 的 output
				step2.addWith("input2", "Hello ${{steps.step_1.outputs.output_key1}}");
				job1.addStep(step2);
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
				when(action1.run()).thenReturn(true);
				when(action1.getOutput(eq("output_key1"))).thenReturn("b");
				when(action1.getOutput("output_key2")).thenReturn("c");
				step1.setUses(action1);
				job1.addStep(step1);
				
				Step step2 = new Step("step_2");
				AbstractAction action2 = mock(AbstractAction.class);
				when(action2.run()).thenReturn(true);
				step2.setUses(action2);
				// 传入表达式，引用 action1 的 output
				step2.addWith("input2", "a ${{steps.step_1.outputs.output_key1}} ${{steps.step_1.outputs.output_key2}}");
				job1.addStep(step2);
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
	
	@Test
	public void run_read_output_expression_not_exist_at_steps_context() {
		Workflow workflow = new Workflow("I_am_a_workflow");
		Job job1 = new Job("job_1");
			Step step1 = new Step("step_1");
			AbstractAction action1 = mock(AbstractAction.class);
			when(action1.run()).thenReturn(true);
			when(action1.getOutput(eq("output_key1"))).thenReturn("world");
			step1.setUses(action1);
			step1.addWith("input2", "Hello ${{steps.step_0.outputs.output_key1}}");
		job1.addStep(step1);
		
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		
		assertThrows(IllegalArgumentException.class, () -> runner.run(workflow));
		verify(action1, never()).run();
	}

	@Test
	public void run_read_output_expression_at_not_supported_context() {
		Workflow workflow = new Workflow("I_am_a_workflow");
		Job job1 = new Job("job_1");
			Step step1 = new Step("step_1");
			AbstractAction action1 = mock(AbstractAction.class);
			when(action1.run()).thenReturn(true);
			when(action1.getOutput(eq("output_key1"))).thenReturn("world");
			step1.setUses(action1);
			step1.addWith("input2", "Hello ${{notSupportContext.step_0.outputs.output_key1}}");
		job1.addStep(step1);
		
		workflow.addJob(job1);
		
		Runner runner = new Runner();
		
		assertThrows(UnsupportedOperationException.class, () -> runner.run(workflow));
		verify(action1, never()).run();
	}
}
