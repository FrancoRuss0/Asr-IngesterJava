package com.kmmaltairlines.demoingester.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BKT06 extends ASRRecord {
	
	private static final DateTimeFormatter SALE_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private LocalDate saleDate;
	
	private BKT06(final String line) {
		super(ASRRecordType.BKT06, line);
	}

	public static BKT06 fromString(final String line) {
		BKT06 record = new BKT06(line);
		
		record.saleDate = LocalDate.parse(line.substring(31, 31 + 10), SALE_DATE_PATTERN);
		
		return record;
	}
	
	public LocalDate getSaleDate() {
		return saleDate;
	}
	
}
