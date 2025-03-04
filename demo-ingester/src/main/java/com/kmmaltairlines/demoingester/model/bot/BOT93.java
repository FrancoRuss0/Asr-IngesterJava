package com.kmmaltairlines.demoingester.model.bot;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BOT93 extends ASRRecord {

	private BOT93(final String line) {
		super(ASRRecordType.BOT93, line);
	}

	public static BOT93 fromString(final String line) {
		BOT93 record = new BOT93(line);
		return record;
	}
	
}
