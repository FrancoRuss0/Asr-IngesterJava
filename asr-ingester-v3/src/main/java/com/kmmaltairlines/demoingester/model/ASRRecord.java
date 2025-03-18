package com.kmmaltairlines.demoingester.model;

import org.springframework.stereotype.Component;

@Component
public abstract class ASRRecord {

	protected String originalLine;
	
	private ASRRecordType recordType;
	private String seqNumber;
	
	public ASRRecord(final ASRRecordType recordType, final String line) {
		this.originalLine = line;
		
		this.recordType = recordType;
		this.seqNumber = line.substring(3, 11);
	}

	@Override
	public String toString() {
		return originalLine;
	}

	public ASRRecordType getRecordType() {
		return recordType;
	}

	public String getSeqNumber() {
		return seqNumber;
	}
}
