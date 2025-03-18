package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS48 extends ASRRecord {
	
	private BKS48(final String line) {
		super(ASRRecordType.BKS48, line);
	}

	public static BKS48 fromString(final String line) {
		BKS48 record = new BKS48(line);
		return record;
	}
	
}
