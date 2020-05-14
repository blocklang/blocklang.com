package com.blocklang.core.util;

import java.time.LocalDateTime;

public class TestBean {
	private String text;
	private int intNumber;
	private double doubleNumber;
	private LocalDateTime date;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getIntNumber() {
		return intNumber;
	}
	public void setIntNumber(int intNumber) {
		this.intNumber = intNumber;
	}
	public double getDoubleNumber() {
		return doubleNumber;
	}
	public void setDoubleNumber(double doubleNumber) {
		this.doubleNumber = doubleNumber;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
}
