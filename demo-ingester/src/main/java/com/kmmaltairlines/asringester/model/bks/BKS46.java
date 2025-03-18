package com.kmmaltairlines.asringester.model.bks;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKS46 extends ASRRecord {
	
	private BKS46(final String line) {
		super(ASRRecordType.BKS46, line);
	}

	public static BKS46 fromString(final String line) {
		BKS46 record = new BKS46(line);
		return record;
	}

}
