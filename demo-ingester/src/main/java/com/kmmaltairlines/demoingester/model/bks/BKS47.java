package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS47 extends ASRRecord {

	private BKS47(final String line) {
		super(ASRRecordType.BKS47, line);
	}

	public static BKS47 fromString(final String line) {
		BKS47 record = new BKS47(line);
		return record;
	}
	
}
