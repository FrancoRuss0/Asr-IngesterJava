package com.kmmaltairlines.asringester.model.bkf;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKF81 extends ASRRecord {
	
	private BKF81(final String line) {
		super(ASRRecordType.BKF81, line);
	}
	
	public static BKF81 fromString(final String line) {
		BKF81 record = new BKF81(line);
		return record;
	}
	
}
