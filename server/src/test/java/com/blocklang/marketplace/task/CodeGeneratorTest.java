package com.blocklang.marketplace.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeGeneratorTest {

	@Test
	public void next_seed_is_null() {
		// 默认产生四位字符串
		CodeGenerator generator = new CodeGenerator(null);
		assertThat(generator.next()).isEqualTo("0001");
		assertThat(generator.next()).isEqualTo("0002");
	}
	
	@Test
	public void next_seed_is_blank_string() {
		// 默认产生四位字符串
		CodeGenerator generator = new CodeGenerator(" ");
		assertThat(generator.next()).isEqualTo("0001");
		assertThat(generator.next()).isEqualTo("0002");
	}
	
	@Test
	public void next_seed_can_not_convert_to_number() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> new CodeGenerator("abcd"));
	}
	
	@Test
	public void next_seed_all_is_zero() {
		// 默认产生四位字符串
		CodeGenerator generator = new CodeGenerator("0000");
		assertThat(generator.next()).isEqualTo("0001");
		assertThat(generator.next()).isEqualTo("0002");
	}
	
	@Test
	public void next_seed_is_valid() {
		// 默认产生四位字符串
		CodeGenerator generator = new CodeGenerator("0003");
		assertThat(generator.next()).isEqualTo("0004");
		assertThat(generator.next()).isEqualTo("0005");
		
		generator = new CodeGenerator("0009");
		assertThat(generator.next()).isEqualTo("0010");
		assertThat(generator.next()).isEqualTo("0011");
	}
	
	@Test
	public void next_seed_out_of_bound() {
		// 默认产生四位字符串
		CodeGenerator generator = new CodeGenerator("9999");
		Assertions.assertThrows(IndexOutOfBoundsException.class, () -> generator.next());
	}

}
