package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS31 extends ASRRecord {

	private BKS31(final String line) {
		super(ASRRecordType.BKS31, line);
	}
	
	public static BKS31 fromString(final String line) {
		BKS31 record = new BKS31(line);
		return record;
	}

}
