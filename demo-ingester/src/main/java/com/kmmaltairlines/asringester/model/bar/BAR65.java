package com.kmmaltairlines.asringester.model.bar;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BAR65 extends ASRRecord {

	private BAR65(final String line) {
		super(ASRRecordType.BAR65, line);
	}

	public static BAR65 fromString(final String line) {
		BAR65 record = new BAR65(line);
		return record;
	}
	
}
