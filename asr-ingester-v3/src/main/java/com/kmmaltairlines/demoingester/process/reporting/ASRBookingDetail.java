package com.kmmaltairlines.demoingester.process.reporting;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ASRBookingDetail {
	
	private static final DateTimeFormatter PROCESS_START_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Europe/Malta"));
	private static final DateTimeFormatter SALE_DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private String formOfPaymentLineNumber;
	private ZonedDateTime formOfPaymentProcessStart = ZonedDateTime.now();
	private String PNR;
	private LocalDate saleDate;
	private String PCC;
	private String paymentMethod;
	private String state;
	private String errorMessage;
	
	public ASRBookingDetail(final String formOfPaymentLineNumber, 
							final String PNR, 
							final LocalDate saleDate, 
							final String PCC, 
							final String paymentMethod,
							final String state, 
							final String errorMessage) {
		
		this.formOfPaymentLineNumber = formOfPaymentLineNumber;
		this.PNR = PNR;
		this.saleDate = saleDate;
		this.PCC = PCC;
		this.paymentMethod = paymentMethod;
		this.state = state;
		this.errorMessage = errorMessage;
	}

	public ZonedDateTime getFormOfPaymentProcessStartAsZonedDateTime() {
		return formOfPaymentProcessStart;
	}

	public String getFormOfPaymentProcessStartAsString() {
		return PROCESS_START_PATTERN.format(formOfPaymentProcessStart);
	}
	
	public String getFormOfPaymentLineNumber() {
		return formOfPaymentLineNumber;
	}

	public String getPNR() {
		return PNR;
	}

	public LocalDate getSaleDateAsLocalDate() {
		return saleDate;
	}
	
	public String getSaleDateAsString() {
		return SALE_DATE_PATTERN.format(saleDate);
	}
	
	public java.sql.Date getSaleDateAsSQLDate() {
		return java.sql.Date.valueOf(saleDate);
	}

	public String getPCC() {
		return PCC;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public String getState() {
		return state;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
