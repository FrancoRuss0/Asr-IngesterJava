package com.kmmaltairlines.asringester.model.bmd;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BMD75 extends ASRRecord {

	private BMD75(final String line) {
		super(ASRRecordType.BMD75, line);
	}

	public static BMD75 fromString(final String line) {
		BMD75 record = new BMD75(line);
		return record;
	}
	
}
