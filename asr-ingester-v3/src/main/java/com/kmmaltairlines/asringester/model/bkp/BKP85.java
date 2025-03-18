package com.kmmaltairlines.asringester.model.bkp;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKP85 extends ASRRecord {

	private BKP85(final String line) {
		super(ASRRecordType.BKP85, line);
	}
	
	public static BKP85 fromString(final String line) {
		BKP85 record = new BKP85(line);
		return record;
	}
	
}
