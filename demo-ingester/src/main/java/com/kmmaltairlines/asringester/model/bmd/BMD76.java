package com.kmmaltairlines.asringester.model.bmd;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BMD76 extends ASRRecord {

	private BMD76(final String line) {
		super(ASRRecordType.BMD76, line);
	}

	public static BMD76 fromString(final String line) {
		BMD76 record = new BMD76(line);
		return record;
	}
	
}
