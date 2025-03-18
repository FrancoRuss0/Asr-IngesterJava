package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS32 extends ASRRecord {
	
	private BKS32(final String line) {
		super(ASRRecordType.BKS32, line);
	}

	public static BKS32 fromString(final String line) {
		BKS32 record = new BKS32(line);
		return record;
	}
	
}
