package com.kmmaltairlines.demoingester.process.reporting;

import java.util.List;

public class ASRReportingUtils {
	
	public static String createASRProcessReport(ASRProcessReport report) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(getASRProcessReportHeading(report));
		
		builder.append(getASRProcessReportBKT84Details(report.getBookingDetails()));
		
		return builder.toString();
	}
	
	private static String getASRProcessReportHeading(ASRProcessReport report) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format("ASRFile: %s\n", report.getFilename()));
		builder.append(String.format("ProcessStart: %s, ProcessEnd: %s\n", 
				report.getProcessStartAsString(), 
				report.getProcessEndAsString()));
		
		return builder.toString();
	}

	private static String getASRProcessReportBKT84Details(List<ASRBookingDetail> bookingDetails) {
	    StringBuilder builder = new StringBuilder();

	    builder.append("BKP/84 Details:\n");
	    for (ASRBookingDetail asrBookingDetail : bookingDetails) {
	        builder.append(String.format(" Line: %s, ProcessStart: %s, PNR: %s, SaleDate: %s, PCC: %s, PaymentMethod: %-5s",
	                asrBookingDetail.getFormOfPaymentLineNumber(),
	                asrBookingDetail.getFormOfPaymentProcessStartAsString(),
	                asrBookingDetail.getPNR(),
	                asrBookingDetail.getSaleDateAsString(),
	                asrBookingDetail.getPCC(),
	                asrBookingDetail.getPaymentMethod() + ","));
	        if (asrBookingDetail.getErrorMessage() != null) {
	            builder.append(String.format(" State: %-10s, ErrorMessage: %s",
	                    asrBookingDetail.getState() + ",",
	                    asrBookingDetail.getErrorMessage()));
	        } else {
	            builder.append(String.format(" State: %-9s", asrBookingDetail.getState()));
	        }
	        builder.append("\n");
	    }

	    return builder.toString();
	}


}
