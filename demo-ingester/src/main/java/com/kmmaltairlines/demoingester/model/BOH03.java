package com.kmmaltairlines.demoingester.model;

public class BOH03 extends ASRRecord {
	
	private String PCC;
	
	private BOH03(final String line) {
		super(ASRRecordType.BOH03, line);
	}
	
	public static BOH03 fromString(final String line) {
		BOH03 record = new BOH03(line);
		record.PCC = line.substring(27, 27 + 3);
		return record;
	}

	public String getPCC() {
		return PCC;
	}
	
}
