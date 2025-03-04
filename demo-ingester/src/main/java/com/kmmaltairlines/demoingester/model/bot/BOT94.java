package com.kmmaltairlines.demoingester.model.bot;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BOT94 extends ASRRecord {
	
	private BOT94(final String line) {
		super(ASRRecordType.BOT94, line);
	}

	public static BOT94 fromString(final String line) {
		BOT94 record = new BOT94(line);
		return record;
	}
	
}
