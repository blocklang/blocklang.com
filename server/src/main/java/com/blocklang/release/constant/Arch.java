package com.blocklang.release.constant;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum Arch {
	
	UNKNOWN("01", "Unknown"),
	X86_64("02", "X86_64"),
	X86("03", "X86"),
	WASM32("04", "WASM32"),
	SPARC64("05", "SPARC64"),
	S390X("06", "S390X"),
	RISCV("07", "RISCV"),
	POWERPC64("08", "POWERPC64"),
	POWERPC("09", "POWERPC"),
	MSP430("10", "MSP430"),
	MIPS64("11", "MIPS64"),
	MIPS("12", "MIPS"),
	ASMJS("13", "ASMJS"),
	ARM("14", "ARM"),
	AARCH64("15", "AARCH64"),
	ANY("99", "Any");

	private final String key;
	private final String value;

	private Arch(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return this.key;
	}

	public String getValue() {
		return this.value;
	}
	
	public static Arch fromValue(String value) {
		if (StringUtils.isBlank(value)) {
			return Arch.UNKNOWN;
		}
		return Arrays.stream(Arch.values())
				.filter((each) -> value.toLowerCase().equals(each.getValue().toLowerCase()))
				.findFirst()
				.orElse(Arch.UNKNOWN);
	}

	public static Arch fromKey(String key) {
		if (StringUtils.isBlank(key)) {
			return Arch.UNKNOWN;
		}
		return Arrays.stream(Arch.values())
				.filter((each) -> key.equals(each.getKey()))
				.findFirst()
				.orElse(Arch.UNKNOWN);
	}
}
