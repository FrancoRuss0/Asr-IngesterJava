package com.kmmaltairlines.demoingester.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.kmmaltairlines.demoingester.exceptions.UnknownRecordTypeException;

public enum ASRRecordType {

    BFH01 ("BFH", "01"),
    BCH02 ("BCH", "02"),
    BOH03 ("BOH", "03"),
    BKT06 ("BKT", "06"),
    BKS24 ("BKS", "24"),
    BKS30 ("BKS", "30"),
    BKS31 ("BKS", "31"),
    BKS32 ("BKS", "32"),
    BKS39 ("BKS", "39"),
    BKS45 ("BKS", "45"),
    BKS46 ("BKS", "46"),
    BKS47 ("BKS", "47"),
    BKS48 ("BKS", "48"),
    BKI63 ("BKI", "63"),
    BAR64 ("BAR", "64"),
    BAR65 ("BAR", "65"),
    BMP70 ("BMP", "70"),
    BKF81 ("BKF", "81"),
    BMD75 ("BMD", "75"),
    BMD76 ("BMD", "76"),
    BKP84 ("BKP", "84"),
    BKP85 ("BKP", "85"),
    BKP86 ("BKP", "86"),
    BKP88 ("BKP", "88"),
    BOT93 ("BOT", "93"),
    BOT94 ("BOT", "94"),
    BFT99 ("BFT", "99"), 
    ;
    
    private static final Map<String,ASRRecordType> MAP = new HashMap<>();
    
    static {
        Stream.of(ASRRecordType.values())
                .forEach(recordType -> {
                    MAP.put(recordType.toString(), recordType);
                });
    }
    
	private String messageId;
	private String numericQualifier;

	private ASRRecordType(final String msgId, final String numericQualifier) {
		this.messageId = msgId;
		this.numericQualifier = numericQualifier;
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public String getNumericQualifier() {
		return numericQualifier;
	}
	
	@Override
	public String toString() {
		return String.format("%s/%s", messageId, numericQualifier);
	}

	/**
	 * Establishes the type of record based on characters at the beginning of a line, specifically the first three
	 * characters (standard message id) and another two after the sequence number (standard numeric qualifier).
	 * 
	 * Example: BKT0000000406TKTE  000000FFP.............
	 * 
	 * BKT will be our standard message id followed by 8 characters that denote the sequence/line number (00000004 in
	 * this case), and the standard numeric qualifier 06 followed by the rest of the data. Therefore the record type 
	 * here is BKT/06.
	 * 
	 * @param line
	 * @return
	 */
	public static ASRRecordType fromString(final String line) {
		String standardMessageId = getMessageId(line);
		String standardNumericQualifier = getNumericQualifier(line);
		
		return Optional.ofNullable(MAP.get(standardMessageId + "/" + standardNumericQualifier))
		                .orElseThrow(() -> new UnknownRecordTypeException(line));
	}

	/**
	 * Get the type of record based on the first three characters at the beginning of a line.
	 * 
	 * Example: BKT0000000406TKTE  000000FFP.............
	 * 
	 * BKT will be our standard message id.
	 * 
	 * @param line
	 * @return
	 */
	public static String getMessageId(final String line) {
	    return line.substring(0, 3);
	}
	
	/**
	 * Get the lineNumber of record based on the characters 4 until 12.
	 * @param line
	 * @return
	 */
	public static int getLineNumber(final String line) {
	    return Integer.parseInt(line.substring(3, 11));
	}
	
	/**
	 * Get the numeric qualifier based on the first 12 and 13 characters after the sequence number.
	 * 
	 * Example: BKT0000000406TKTE  000000FFP.............
	 * 
	 * Here is 06.
	 * 
	 * @param line
	 * @return
	 */
    public static String getNumericQualifier(final String line) {
        return line.substring(11, 13);
    }
    
	
}
