package com.kmmaltairlines.demoingester.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.lang3.exception.ExceptionUtils;

public class Utility {
	
	public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
	
	public static Throwable rootCause(Exception exception) {
		Throwable rootCause = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(exception);
		if (rootCause == null) {
			return exception;
		}
		else return rootCause;
	}
	
//	createBookingDetailReporting() {
//		return new com.airmalta.hip.asringester.process.reporting.ASRBookingDetail(
//			payload.seqNumber, 
//			flowVars.stationTransaction.PNR, 
//			flowVars.stationTransaction.saleDateAsLocalDate,
//			flowVars.stationTransaction.PCC, 
//			payload.formOfPaymentType, 
//			flowVars.asrReportingState, 
//			flowVars.asrReportingErrorMessage); 
//	}
}
