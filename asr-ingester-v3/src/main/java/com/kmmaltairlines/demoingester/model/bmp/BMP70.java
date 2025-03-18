package com.kmmaltairlines.demoingester.model.bmp;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BMP70 extends ASRRecord {
	
	private BMP70(final String line) {
		super(ASRRecordType.BMP70, line);
	}

	public static BMP70 fromString(final String line) {
		BMP70 record = new BMP70(line);
		return record;
	}

}
