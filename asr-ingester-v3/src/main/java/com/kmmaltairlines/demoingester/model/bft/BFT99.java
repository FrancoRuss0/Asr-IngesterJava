package com.kmmaltairlines.demoingester.model.bft;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BFT99 extends ASRRecord {
	
	private BFT99(final String line) {
		super(ASRRecordType.BFT99, line);
	}

	public static BFT99 fromString(final String line) {
		BFT99 record = new BFT99(line);
		return record;
	}
	
}
