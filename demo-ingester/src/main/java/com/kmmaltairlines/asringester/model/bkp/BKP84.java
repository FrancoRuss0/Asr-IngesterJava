package com.kmmaltairlines.asringester.model.bkp;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.kmmaltairlines.asringester.model.ASRRecord;
import com.kmmaltairlines.asringester.model.ASRRecordType;

public class BKP84 extends ASRRecord {
	
	private String formOfPaymentType;
	private String authCode;
	private BigDecimal trxAmount;
	private String cardNumber;
	
	private static final BigDecimal HUNDRED = new BigDecimal(100);
	
	private BKP84(final String line) {
		super(ASRRecordType.BKP84, line);
	}
	
	public static BKP84 fromString(final String line) {
		BKP84 record = new BKP84(line);
		record.formOfPaymentType = line.substring(25, 25 + 10).trim(); // Why does this have 10 characters ... ?
		record.authCode = line.substring(74, 74 + 6).trim();
		
		String rawFOPAmount = line.substring(35, 35 + 11).trim().toUpperCase();
		record.trxAmount = parseAmount(rawFOPAmount);
		
		record.cardNumber = line.substring(46, 46 + 19);
		
		return record;
	}
	
	/**
	 * This method is used to extract the price of the sale from a subset of the BKP/84 line.
	 * It is assumed that this line will always have:
	 *  - 11 characters in total
	 *  - The first 10 characters being a number
	 *  - The 11th character being a letter, which is then translated to a number depending on its alphabetical index.
	 *  
	 * Taking the following as an example: "0000004362C", this is translated to a value of 436.23.
	 * Given that 'C' is the 3rd letter of the alphabet, it is translated to a value of 3.
	 * The exception to the rule is the '{' character, which translates to 0.
	 * 
	 * @param rawFOPAmount The subset string of the BKP/84 line containing the price.
	 * @return
	 */
	private static BigDecimal parseAmount(final String rawFOPAmount) {
		int rawFOPAmountWithoutLastNumber = Integer.parseInt(rawFOPAmount.toUpperCase().substring(0, 10)) * 10; // Multiply by 10 since we want to add the last digit later.
		char lastNumberAsCharacter = rawFOPAmount.charAt(10);
		
		// TODO: Check if there is any limitation on the letters that can go in here. 
		// Max value for a single digit letter is 'I', which is the 9th letter of the alphabet.
		// "J" would be a 10, however 0 is represented by '{'. 
		// Does this mean we add 10c to the total, or can the maximum letter that this line can ever have is 'I'?
		int lastCharacterInCents;
		if (lastNumberAsCharacter == '{') {
			lastCharacterInCents = 0;
		}
		else lastCharacterInCents = Character.getNumericValue(lastNumberAsCharacter) - Character.getNumericValue('A') + 1;
		
		int fopAmountInCents = rawFOPAmountWithoutLastNumber + lastCharacterInCents;
		
		return BigDecimal.valueOf(fopAmountInCents).divide(HUNDRED);
	}
	
	/**
	 * Checks whether the form of payment is a credit or debit card payment.
	 * @return true for card payments, otherwise false
	 */
	public boolean isCardPayment() {
		return (formOfPaymentType.startsWith("CC") || formOfPaymentType.startsWith("XX"));
	}
	
	public void setCardNumber(final String cardNumber) {
		Validate.isTrue(cardNumber.length() <= 19, "Card Number cannot be larger than 19 alphanumeric characters.");

		this.cardNumber = StringUtils.rightPad(cardNumber, 19);
	}
	
	@Override
	public String toString() {
		return originalLine.substring(0, 46) + this.cardNumber + originalLine.substring(65);
	}
	
	public String getFormOfPaymentType() {
		return formOfPaymentType;
	}
	
	public String getAuthCode() {
		return authCode;
	}
	
	public BigDecimal getTrxAmount() {
		return trxAmount;
	}

	public String getCardNumber() {
		return cardNumber;
	}

}
