package com.kmmaltairlines.asringester.model.bkp;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKP88 extends ASRRecord {

	private BKP88(final String line) {
		super(ASRRecordType.BKP88, line);
	}
	
	public static BKP88 fromString(final String line) {
		BKP88 record = new BKP88(line);
		return record;
	}
	
}
