package com.kmmaltairlines.asringester.model.bks;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKS39 extends ASRRecord {

	private BKS39(final String line) {
		super(ASRRecordType.BKS39, line);
	}

	public static BKS39 fromString(String line) {
		BKS39 record = new BKS39(line);
		return record;
	}

}
