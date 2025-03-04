package com.kmmaltairlines.demoingester.payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kmmaltairlines.demoingester.model.StationTransaction;
import com.kmmaltairlines.demoingester.model.StationTransactionGroup;
import com.kmmaltairlines.demoingester.model.bkp.BKP84;

@Service
public class APCOPaymentProcessor {
	String asrReportingState;
	String asrReportingErrorMessage;
	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private final DataSource dataSource;

	public APCOPaymentProcessor(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	// select APCO payments lookup
	public List<Map<String, Object>> lookupAPCOPayment(String pnr, String authCode, java.sql.Date saleDate,
			BigDecimal combinedTrxAmount) {
		
		List<Map<String, Object>> results = new ArrayList<>();
		
		if (pnr == null || authCode == null || saleDate == null || combinedTrxAmount == null) {
			log.error("Error: One of the parameter is null.");
			return null;
		}
		String query = "SELECT PNR, AMOUNT, AUTHCODE, CARDNUM2 FROM PARIS.APCO_PAYMENTS WHERE TRNTYPE = 'AUTH' "
				+ "AND PNR = ? AND AUTHCODE = ? AND TRNDATE = ? AND AMOUNT = ?";

		try (Connection connection = dataSource.getConnection();
				PreparedStatement stmt = connection.prepareStatement(query)) {

			stmt.setString(1, pnr);
			stmt.setString(2, authCode);
			stmt.setDate(3, saleDate);
			stmt.setBigDecimal(4, combinedTrxAmount);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Map<String, Object> result = new HashMap<>();
					result.put("PNR", rs.getString("PNR"));
					result.put("AMOUNT", rs.getString("AMOUNT"));
					result.put("AUTHCODE", rs.getString("AUTHCODE"));
					result.put("CARDNUM2", rs.getString("CARDNUM2"));
					results.add(result);
				}
			}
		} catch (SQLException e) {
			log.error("Error while looking up APCO payment: {}", e.getMessage());
		}
		return results;
	}

	// lookup-APCO-payment-subflow
	// TO-DO: rivedere
	public void processAPCOPayment(StationTransaction trx, BKP84 form) {
		if (trx == null || form == null) {
			log.error("Error: One of the parameter is null.");
			return;
		}

		String pnr = trx.getPNR();
		StationTransactionGroup firstGroup = trx.getStationRecord().getStationTransactionGroups().values().stream().findFirst().orElse(null);
		String authCode = (firstGroup != null) ? firstGroup.getAuthCode() : null;

		if (authCode != null) {
//			StationTransactionGroup group = trx.getStationRecord().getStationTransactionGroups()
//					.get(String.format("%s-%s", pnr, authCode));
			
			java.sql.Date saleDate = trx.getSaleDateAsSQLDate();

			// select APCO payments lookup: set paymentLookup
			log.info("Performing lookup against APCO records for PNR {} and AuthCode {}, Sale Date: {}.", pnr, authCode, saleDate);
			
			// getCombinedTotal
			BigDecimal combinedTrxAmount = firstGroup.getCombinedTotal();

			// Select: APCO payments lookup
			List<Map<String, Object>> paymentLookup = lookupAPCOPayment(pnr, authCode, saleDate, combinedTrxAmount);

			if (paymentLookup != null && !paymentLookup.isEmpty()) {
				modifyCardNumber(paymentLookup.get(0), form);
				this.asrReportingState = "CHANGED";
				this.asrReportingErrorMessage = null;
			} else {
				log.error("Could not find PNR {} against APCO payments, with AuthCode {} and Sale Date {}.", pnr, authCode, saleDate);
				this.asrReportingState = "MISMATCH";
				this.asrReportingErrorMessage = "Failed to find a matching APCO payment in APCO_PAYMENTS. The APCO payments file might not have been loaded yet.";
				return;
			}
		} else {
			log.error("Error: AuthCode is null.");
			return;
		}
	}

	// alter-card-payment-method-flow
	public void modifyCardNumber(Map<String, Object> paymentLookup, BKP84 bkp) {
		String cardNum2 = (String) paymentLookup.get("CARDNUM2");
		String cardNumber = bkp.getCardNumber();

		// choice: does cardNumber exist?
		if (StringUtils.isNotEmpty(cardNum2) && StringUtils.isNotEmpty(cardNumber) && cardNumber != null) {
			if (cardNum2.length() >= 2 && cardNumber.length() >= 2) {
				// set cardNumber
				bkp.setCardNumber(cardNum2.substring(0, 2) + cardNumber.substring(2));
			}
		} else {
			log.error("CardNumber variable is null.");
		}
	}

	public String getAsrReportingState() {
		return asrReportingState;
	}

	public void setAsrReportingState(String asrReportingState) {
		this.asrReportingState = asrReportingState;
	}

	public String getAsrReportingErrorMessage() {
		return asrReportingErrorMessage;
	}

	public void setAsrReportingErrorMessage(String asrReportingErrorMessage) {
		this.asrReportingErrorMessage = asrReportingErrorMessage;
	}
	
}
