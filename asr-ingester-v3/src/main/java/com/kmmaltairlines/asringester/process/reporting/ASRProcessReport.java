package com.kmmaltairlines.asringester.process.reporting;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ASRProcessReport {

	private static final DateTimeFormatter PROCESS_START_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.of("Europe/Malta"));
	private static final DateTimeFormatter PROCESS_END_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.of("Europe/Malta"));

	private String filename;
	private ZonedDateTime processStart = ZonedDateTime.now();
	private ZonedDateTime processEnd;
	private List<ASRBookingDetail> bookingDetails = new ArrayList<>();

	public ASRProcessReport() {

	}

	public ASRProcessReport(final String filename) {
		this.filename = filename;
	}

	public void addBookingDetail(final ASRBookingDetail bookingDetail) {
		bookingDetails.add(bookingDetail);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public ZonedDateTime getProcessStartAsZonedDateTime() {
		return processStart;
	}

	public String getProcessStartAsString() {
		return PROCESS_START_PATTERN.format(processStart);
	}

	public ZonedDateTime getProcessEndAsZonedDateTime() {
		return processEnd;
	}

	public String getProcessEndAsString() {
		if (this.processEnd == null) {
			return "Process End not set";
		}
		return PROCESS_END_PATTERN.format(processEnd);
	}

	public void setProcessEnd(ZonedDateTime processEnd) {
		this.processEnd = processEnd;
	}

	public List<ASRBookingDetail> getBookingDetails() {
		return bookingDetails;
	}

}
