package com.kmmaltairlines.asringester.model.bkp;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKP86 extends ASRRecord {

	private BKP86(final String line) {
		super(ASRRecordType.BKP86, line);
	}
	
	public static BKP86 fromString(final String line) {
		BKP86 record = new BKP86(line);
		return record;
	}
	
}
