package com.blocklang.marketplace.runner.action;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 测试名称遵循 {order}__{description} 命名规范，并分别解析出 order 和 description
 * 
 * @author Zhengwei Jin
 *
 */
public class ApiRepoPathReaderTest {

	@Test
	public void validate() {
		ApiRepoPathReader reader = new ApiRepoPathReader();
		
		assertThat(reader.validate("1")).hasSize(1);
		assertThat(reader.validate("202005200808__description1")).isEmpty();
		assertThat(reader.validate("202005200808")).isEmpty();
		assertThat(reader.validate("202005200808__")).isEmpty();
		assertThat(reader.validate("202005200808__a__b_c")).isEmpty();
	}
	
	@Test
	public void read() {
		ApiRepoPathReader reader = new ApiRepoPathReader();
		
		assertThat(reader.read("1")).isNull();
		
		assertThat(reader.read("202005200808__description1"))
			.hasFieldOrPropertyWithValue("order", "202005200808")
			.hasFieldOrPropertyWithValue("description", "description1");
		
		assertThat(reader.read("202005200808"))
			.hasFieldOrPropertyWithValue("order", "202005200808")
			.hasFieldOrPropertyWithValue("description", null);
		
		assertThat(reader.read("202005200808__"))
			.hasFieldOrPropertyWithValue("order", "202005200808")
			.hasFieldOrPropertyWithValue("description", null);
		
		assertThat(reader.read("202005200808__a__b_c"))
		.hasFieldOrPropertyWithValue("order", "202005200808")
		.hasFieldOrPropertyWithValue("description", "a__b_c");
	}
	
	@Test
	public void validate_then_read() {
		ApiRepoPathReader reader = new ApiRepoPathReader();
		
		assertThat(reader.validate("1")).hasSize(1);
		assertThat(reader.read("1")).isNull();
	}
	
}
