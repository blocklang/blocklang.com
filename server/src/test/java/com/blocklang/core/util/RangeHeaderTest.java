package com.blocklang.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Range;

public class RangeHeaderTest {
	
	@Test
	public void is_valid() {
		assertThat(RangeHeader.isValid(null)).isFalse();
		assertThat(RangeHeader.isValid("")).isFalse();
		assertThat(RangeHeader.isValid(" ")).isFalse();
		assertThat(RangeHeader.isValid("x")).isFalse();
		assertThat(RangeHeader.isValid("x=")).isFalse();
		assertThat(RangeHeader.isValid("bytes=1-")).isTrue();
		assertThat(RangeHeader.isValid("bytes=200-1000,2000-6576,19000-")).isTrue();
		assertThat(RangeHeader.isValid("bytes=200-1000, 2000-6576,  19000-")).isTrue();
	}

	@Test
	public void parse_range_header_is_null() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> RangeHeader.parse(null, 10L));
	}
	
	@Test
	public void parse_range_header_is_blank() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> RangeHeader.parse(" ", 10L));
	}
	
	@Test
	public void parse_range_header_not_contains_range() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> RangeHeader.parse("xxx=-1 ", 10L));
	}
	
	@Test
	public void parse_range_header_not_contains_strikethrough() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> RangeHeader.parse("xxx=1 ", 10L));
	}
	
	@Test
	public void parse_not_has_lower_or_upper() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> RangeHeader.parse("bytes=-", 10L));
	}
	
	@Test
	public void parse_not_number() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> RangeHeader.parse("bytes=a-b", 10L));
	}
	
	@Test
	public void parse_starts_with_strikethrough() {
		Range<Long> range = RangeHeader.parse("bytes=-1", 10L);
		
		assertThat(range.getLowerBound().getValue().get()).isEqualTo(0);
		assertThat(range.getUpperBound().getValue().get()).isEqualTo(1);
	}
	
	@Test
	public void parse_ends_with_strikethrough() {
		Range<Long> range = RangeHeader.parse("bytes=1-", 10L);
		
		assertThat(range.getLowerBound().getValue().get()).isEqualTo(1);
		assertThat(range.getUpperBound().getValue().get()).isEqualTo(10);
	}
	
	@Test
	public void parse_has_lower_and_upper() {
		Range<Long> range = RangeHeader.parse("bytes=1-2", 10L);
		
		assertThat(range.getLowerBound().getValue().get()).isEqualTo(1);
		assertThat(range.getUpperBound().getValue().get()).isEqualTo(2);
	}
	
}
