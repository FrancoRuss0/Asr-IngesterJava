package com.kmmaltairlines.demoingester.model.bmd;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BMD75 extends ASRRecord {

	private BMD75(final String line) {
		super(ASRRecordType.BMD75, line);
	}

	public static BMD75 fromString(final String line) {
		BMD75 record = new BMD75(line);
		return record;
	}
	
}
