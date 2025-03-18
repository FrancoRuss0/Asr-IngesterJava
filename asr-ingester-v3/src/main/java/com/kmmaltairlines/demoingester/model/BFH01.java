package com.kmmaltairlines.demoingester.model;

public class BFH01 extends ASRRecord {
	
	private BFH01(final String line) {
		super(ASRRecordType.BFH01, line);
	}
	
	public static BFH01 fromString(final String line) {
		BFH01 record = new BFH01(line);
		return record;
	}

}
