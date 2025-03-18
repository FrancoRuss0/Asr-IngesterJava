package com.kmmaltairlines.asringester.model;

public class BCH02 extends ASRRecord {

	private BCH02(final String line) {
		super(ASRRecordType.BCH02, line);
	}
	
	public static BCH02 fromString(final String line) {
		BCH02 record = new BCH02(line);
		return record;
	}
	
}
