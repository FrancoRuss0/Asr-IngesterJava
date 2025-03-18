package com.kmmaltairlines.asringester.model.bks;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKS47 extends ASRRecord {

	private BKS47(final String line) {
		super(ASRRecordType.BKS47, line);
	}

	public static BKS47 fromString(final String line) {
		BKS47 record = new BKS47(line);
		return record;
	}
	
}
