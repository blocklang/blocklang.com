package com.blocklang.core.util;

import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import com.nimbusds.oauth2.sdk.util.StringUtils;


public class RangeHeader {

	public static Range<Long> parse(String rangeHeader, Long inclusiveUpperBound) {
		Assert.hasText(rangeHeader, "range header 不能为空");
		Assert.isTrue(rangeHeader.startsWith("range="), "range header 应该以 range= 开头");
		Assert.isTrue(rangeHeader.contains("-"), "range header 应该包含 -");
		
		String trimedRange = rangeHeader.trim().substring("range=".length());

		String[] ranges = trimedRange.split("-");
		Assert.isTrue(ranges.length > 0, "至少包含一个片段");
		
		long start = 0;
		long end = 0;
		
		// "range=1-" 排除 range=
		if (ranges.length == 1) {
			start = NumberUtils.parseNumber(ranges[0], Long.class);
			end = inclusiveUpperBound;
		} else if (ranges.length == 2) {
			if (StringUtils.isBlank(ranges[0])) {
				start = 0L;
			} else {
				start = NumberUtils.parseNumber(ranges[0], Long.class);
			}
			end = NumberUtils.parseNumber(ranges[1], Long.class);
		}

		return Range.of(Bound.inclusive(start), Bound.inclusive(end));
	}

}
