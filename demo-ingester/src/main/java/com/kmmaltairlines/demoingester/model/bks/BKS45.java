package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS45 extends ASRRecord {
	
	private BKS45(final String line) {
		super(ASRRecordType.BKS45, line);
	}

	public static BKS45 fromString(final String line) {
		BKS45 record = new BKS45(line);
		return record;
	}
	
}
