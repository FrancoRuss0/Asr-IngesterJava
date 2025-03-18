package com.kmmaltairlines.asringester.model.bki;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKI63 extends ASRRecord {
	
	private String originAirport;
	private String destinationAirport;

	private BKI63(final String line) {
		super(ASRRecordType.BKI63, line);
	}
	
	public static BKI63 fromString(final String line) {
		BKI63 record = new BKI63(line);
		record.originAirport = line.substring(60, 60 + 5).trim();  // TODO: IATA codes are 3 characters, so why 5?
		record.destinationAirport = line.substring(65, 65 + 5).trim();  // TODO: IATA codes are 3 characters, so why 5?
		return record;
	}
	
	public String getOriginAirport() {
		return originAirport;
	}
	
	public String getDestinationAirport() {
		return destinationAirport;
	}

}
