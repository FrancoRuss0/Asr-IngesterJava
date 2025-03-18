package com.kmmaltairlines.asringester.model.bar;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BAR64 extends ASRRecord {
	
	private BAR64(final String line) {
		super(ASRRecordType.BAR64, line);
	}
	
	public static BAR64 fromString(final String line) {
		BAR64 record = new BAR64(line);
		return record;
	}
	
}
