package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS30 extends ASRRecord {
	
	private BKS30(final String line) {
		super(ASRRecordType.BKS30, line);
	}
	
	public static BKS30 fromString(final String line) {
		BKS30 record = new BKS30(line);
		return record;
	}

}
