package com.kmmaltairlines.demoingester.model.bmd;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BMD76 extends ASRRecord {

	private BMD76(final String line) {
		super(ASRRecordType.BMD76, line);
	}

	public static BMD76 fromString(final String line) {
		BMD76 record = new BMD76(line);
		return record;
	}
	
}
