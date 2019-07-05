package com.blocklang.marketplace.task;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.Assert;

import com.nimbusds.oauth2.sdk.util.StringUtils;

public class CodeGenerator {

	private int intSeed;
	
	public CodeGenerator(String seed) {
		if(StringUtils.isBlank(seed)) {
			this.intSeed = 0;
			return;
		}
		Assert.isTrue(NumberUtils.isDigits(seed), "必须是有效的数字");
		
		this.intSeed = NumberUtils.toInt(seed);
	}

	public String next() {
		this.intSeed++;
		if(this.intSeed > 9999) {
			throw new IndexOutOfBoundsException();
		}
		return String.format("%04d", this.intSeed);
	}

}
