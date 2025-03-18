package com.kmmaltairlines.demoingester.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * This class is used to group a number of {@link StationTransaction} by PNR and authCode. This will allow us to match transactions  
 * belonging to the same PNR and having the same authorization code with the respective transaction record on the payment gateway
 * after calculating the total transaction amount for all card payments. This is necessary since the payment gateway only records the 
 * total instead of each transaction amount.
 * 
 * @author Jeffrey Cassar
 *
 */
public class StationTransactionGroup {
	
	private String PNR;
	private String authCode;
	private List<StationTransaction> stationTransactions;

	public StationTransactionGroup(String PNR, String authCode, List<StationTransaction> stationTransactions) {
		this.PNR = PNR;
		this.authCode = authCode;
		this.stationTransactions = stationTransactions;
	}

	/**
	 * Adds up the transaction amount for all card payments of all transactions within the group.
	 * @return The total transaction amount across the group.
	 */
	public BigDecimal getCombinedTotal() {
		return stationTransactions.stream().map(tx -> tx.getCardPaymentsTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
	
	public String getPNR() {
		return PNR;
	}

	public String getAuthCode() {
		return authCode;
	}

	public List<StationTransaction> getStationTransactions() {
		return stationTransactions;
	}
	
}
