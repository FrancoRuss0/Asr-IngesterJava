package com.kmmaltairlines.demoingester.model.bkp;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKP88 extends ASRRecord {

	private BKP88(final String line) {
		super(ASRRecordType.BKP88, line);
	}
	
	public static BKP88 fromString(final String line) {
		BKP88 record = new BKP88(line);
		return record;
	}
	
}
