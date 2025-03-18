package com.kmmaltairlines.asringester.exceptions;

import com.kmmaltairlines.asringester.model.ASRRecordType;

public class UnknownRecordTypeException extends RuntimeException {

	private static final long serialVersionUID = 7944973228793447441L;
    private final String unknownMessageId;
    private final String numericQualifier;
    private final int failedLineNumber;
	
    public UnknownRecordTypeException(final String line) {
        this(ASRRecordType.getMessageId(line), ASRRecordType.getNumericQualifier(line), ASRRecordType.getLineNumber(line));
    }
    
	public UnknownRecordTypeException(final String messageId, final String numericQualifier, final int failedLineNumber) {
	    super(String.format("Unknown record type: %s%s. Failed line number: %d",
	                   messageId, numericQualifier, failedLineNumber));

	    this.unknownMessageId = messageId;
	    this.numericQualifier = numericQualifier;
	    this.failedLineNumber = failedLineNumber;
    }

    public String getUnknownMessageId() {
        return unknownMessageId;
    }

    public String getNumericQualifier() {
        return numericQualifier;
    }

    public int getFailedLineNumber() {
        return failedLineNumber;
    }

}
