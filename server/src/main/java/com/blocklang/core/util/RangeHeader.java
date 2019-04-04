package com.blocklang.core.util;

import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import com.nimbusds.oauth2.sdk.util.StringUtils;


public class RangeHeader {
	
	public static boolean isValid(String rangeHeader) {
		if(StringUtils.isBlank(rangeHeader)) {
			return false;
		}
		return rangeHeader.matches("^bytes=\\d*-\\d*(,\\s*\\d*-\\d*)*$");
	}

	public static Range<Long> parse(String rangeHeader, Long inclusiveUpperBound) {
		Assert.hasText(rangeHeader, "range header 不能为空");
		Assert.isTrue(isValid(rangeHeader), "不是有效的 range header");
		
		String trimedRange = rangeHeader.trim().substring("bytes=".length());

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
