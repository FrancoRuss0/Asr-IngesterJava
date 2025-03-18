package com.kmmaltairlines.asringester.payment;

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

import com.kmmaltairlines.asringester.model.StationTransaction;
import com.kmmaltairlines.asringester.model.StationTransactionGroup;
import com.kmmaltairlines.asringester.model.bkp.BKP84;

@Service
public class LookupPayments {
	String asrReportingState;
	String asrReportingErrorMessage;
	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private final DataSource dataSource;

	public LookupPayments(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	// select APCO payments lookup -> query per ottenere i campi utili
	public List<Map<String, Object>> lookupAPCOPayment(String pnr, String authCode, java.sql.Date saleDate,
			BigDecimal combinedTrxAmount) {

		List<Map<String, Object>> results = new ArrayList<>();
		Connection connection = null;

		if (pnr == null || authCode == null || saleDate == null || combinedTrxAmount == null) {
			log.error("Error: One of the parameter is null.");
			return null;
		}
		String query = "SELECT PNR, AMOUNT, AUTHCODE, CARDNUM2 FROM PARIS.APCO_PAYMENTS WHERE TRNTYPE = 'AUTH' "
				+ "AND PNR = ? AND AUTHCODE = ? AND TRNDATE = ? AND AMOUNT = ?";

		try {
			// apertura connessione
			connection = dataSource.getConnection();

			try (PreparedStatement stmt = connection.prepareStatement(query)) {

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
			}
		} catch (SQLException e) {
			log.error("Error while looking up APCO payment: {}.", e.getMessage());
		} finally {
			// chiusura connessione
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					log.error("Error closing the database connection: {}", e.getMessage());
				}
			}
		}

		return results;
	}

	// lookup-APCO-payment-subflow
	public void processApcoPayment(StationTransaction trx, BKP84 form) {
		if (trx == null || form == null) {
			log.error("Error: One of the parameter is null.");
			return;
		}

		String pnr = trx.getPNR();
		StationTransactionGroup firstGroup = trx.getStationRecord().getStationTransactionGroups().values().stream()
				.findFirst().orElse(null);
		String authCode = (firstGroup != null) ? firstGroup.getAuthCode() : null;

		if (authCode != null) {

			java.sql.Date saleDate = trx.getSaleDateAsSQLDate();

			// select APCO payments lookup: set paymentLookup
			log.info("Performing lookup against APCO records for PNR {} and AuthCode {}, Sale Date: {}.", pnr, authCode,
					saleDate);

			// getCombinedTotal
			BigDecimal combinedTrxAmount = firstGroup.getCombinedTotal();

			// Select: APCO payments lookup ->se la transazione viene trovata, vengono modificate le prime 2 cifre di cardNumber 
			// e il valore di State viene aggiornato con CHANGED, altrimenti State viene aggiornato con MISMATCH
			List<Map<String, Object>> paymentLookup = lookupAPCOPayment(pnr, authCode, saleDate, combinedTrxAmount);

			if (paymentLookup != null && !paymentLookup.isEmpty()) {
				modifyCardNumber(paymentLookup.get(0), form);
				setAsrReportingState("CHANGED");
				setAsrReportingErrorMessage(null);
			} else {
				log.error("Could not find PNR {} against APCO payments, with AuthCode {} and Sale Date {}.", pnr,
						authCode, saleDate);
				setAsrReportingState("MISMATCH");
				setAsrReportingErrorMessage(
						"Failed to find a matching APCO payment in APCO_PAYMENTS. The APCO payments file might not have been loaded yet.");

				return;
			}
		} else {
			log.error("Error: AuthCode is null.");
			return;
		}
	}
	
	public void handleOGONEPayment(StationTransaction trx) {
		log.info("Handling OGONE Payment for PNR: {}.", trx.getPNR());
	}

	// TODO: implementare per i pagamenti OGONE (la logica dovrebbe essere la stessa di APCO)
//	public void handleOGONEPayment(StationTransaction trx) {
//		log.info("Handling OGONE Payment for PNR: {}.", trx.getPNR());
//	}
//	
//	// select OGONE payments lookup -> query per ottenere i campi utili
//	public List<Map<String, Object>> lookupOgonePayment(String authCode, java.sql.Date saleDate,
//			BigDecimal combinedTrxAmount) {
//
//		List<Map<String, Object>> results = new ArrayList<>();
//		Connection connection = null;
//
//		if (authCode == null || saleDate == null || combinedTrxAmount == null) {
//			log.error("Error: One of the parameter is null.");
//			return null;
//		}
//		String query = "SELECT ACCEPT, ORDER_DATE, TOTAL, BINCARD, CARD FROM PARIS.OGONE_PAYMENTS WHERE STATUS = '5' "
//				+ "AND ACCEPT = ? AND ORDER_DATE = ? AND TOTAL = ?";
//
//		try {
//			// apertura connessione
//			connection = dataSource.getConnection();
//
//			try (PreparedStatement stmt = connection.prepareStatement(query)) {
//
//				stmt.setString(1, authCode);
//				stmt.setDate(2, saleDate);
//				stmt.setBigDecimal(3, combinedTrxAmount);
//
//				try (ResultSet rs = stmt.executeQuery()) {
//					while (rs.next()) {
//						Map<String, Object> result = new HashMap<>();
//						result.put("PNR", rs.getString("PNR"));
//						result.put("BINCARD", rs.getString("BINCARD"));
//						result.put("AUTHCODE", rs.getString("AUTHCODE"));
//						result.put("CARDNUM2", rs.getString("CARDNUM2"));
//						results.add(result);
//					}
//				}
//			}
//		} catch (SQLException e) {
//			log.error("Error while looking up APCO payment: {}.", e.getMessage());
//		} finally {
//			// chiusura connessione
//			if (connection != null) {
//				try {
//					connection.close();
//				} catch (SQLException e) {
//					log.error("Error closing the database connection: {}", e.getMessage());
//				}
//			}
//		}
//
//		return results;
//	}
//
//	// lookup-OGONE-payment-subflow
//	public void processOgonePayment(StationTransaction trx, BKP84 form) {
//		if (trx == null || form == null) {
//			log.error("Error: One of the parameter is null.");
//			return;
//		}
//
//		String pnr = trx.getPNR();
//		StationTransactionGroup firstGroup = trx.getStationRecord().getStationTransactionGroups().values().stream()
//				.findFirst().orElse(null);
//		String authCode = (firstGroup != null) ? firstGroup.getAuthCode() : null;
//
//		if (authCode != null) {
//
//			java.sql.Date saleDate = trx.getSaleDateAsSQLDate();
//
//			// select APCO payments lookup: set paymentLookup
//			log.info("Performing lookup against OGONE records for PNR {} and AuthCode {}, Sale Date: {}.", pnr, authCode,
//					saleDate);
//
//			// getCombinedTotal
//			BigDecimal combinedTrxAmount = firstGroup.getCombinedTotal();
//
//			// Select: APCO payments lookup ->se la transazione viene trovata, vengono modificate le prime 2 cifre di cardNumber 
//			// e il valore di State viene aggiornato con CHANGED, altrimenti State viene aggiornato con MISMATCH
//			List<Map<String, Object>> paymentLookup = lookupOgonePayment(authCode, saleDate, combinedTrxAmount);
//
//			if (paymentLookup != null && !paymentLookup.isEmpty()) {
//				modifyCardNumber(paymentLookup.get(0), form);
//				setAsrReportingState("CHANGED");
//				setAsrReportingErrorMessage(null);
//			} else {
//				log.error("Could not find PNR {} against APCO payments, with AuthCode {} and Sale Date {}.", pnr,
//						authCode, saleDate);
//				setAsrReportingState("MISMATCH");
//				setAsrReportingErrorMessage(
//						"Failed to find a matching APCO payment in APCO_PAYMENTS. The APCO payments file might not have been loaded yet.");
//
//				return;
//			}
//		} else {
//			log.error("Error: AuthCode is null.");
//			return;
//		}
//	}

	public void modifyCardNumber(Map<String, Object> paymentLookup, BKP84 bkp) {
		String cardNum2 = (String) paymentLookup.get("CARDNUM2");
		String cardNumber = bkp.getCardNumber();

		// choice: does cardNumber exist?
		if (StringUtils.isNotEmpty(cardNum2) && StringUtils.isNotEmpty(cardNumber) && cardNumber != null) {
			if (cardNum2.length() >= 2 && cardNumber.length() >= 2) {
				// aggiorna il valore di cardNumber, sostituendo le prime due cifre del CardNumber con cardNum2
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
