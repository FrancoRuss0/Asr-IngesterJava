package com.kmmaltairlines.demoingester.model.bks;

import com.kmmaltairlines.demoingester.model.ASRRecord;
import com.kmmaltairlines.demoingester.model.ASRRecordType;

public class BKS24 extends ASRRecord {

	private static final String CONJUNCTION_TICKET_INDICATOR = "CNJ";

	private String PNR;
	
	private BKS24(final String line) {
		super(ASRRecordType.BKS24, line);
	}

	public static BKS24 fromString(final String line) {
		BKS24 record = new BKS24(line);
		record.PNR = line.substring(114, 114 + 13).trim(); // why does a PNR have 13 characters ... ?
		
		return record;
	}
	
	/**
	 * Indicates whether this BKS/24 line is a conjunction ticket or not.
	 * @return true if conjunction ticket, otherwise false
	 */
	public boolean isConjunction() {
		return originalLine.substring(61, 64).equals(CONJUNCTION_TICKET_INDICATOR);
	}
	
	public String getPNR() {
		return PNR;
	}

}
