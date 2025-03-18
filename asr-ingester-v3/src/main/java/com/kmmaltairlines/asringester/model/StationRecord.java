package com.kmmaltairlines.asringester.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.kmmaltairlines.asringester.model.bot.BOT93;
import com.kmmaltairlines.asringester.model.bot.BOT94;

public class StationRecord {
	
	// BOH/03 line
	private BOH03 stationHeader;
	
	// BKT/06 lines and others
	private List<StationTransaction> stationTransactions = new ArrayList<>();
	
	// BOT/93 line
	private BOT93 stationTotals;

	// BOT/94 line
	private BOT94 stationTotalsPerCurrency;

	private Map<String, StationTransactionGroup> stationTransactionGroups = new HashMap<>();
	
	private ASRReport asrReport;

	// A station record must always be in the context of an ASR report
	private StationRecord(ASRReport parentReport) {
		this.asrReport = parentReport;
	}
	
	/**
	 * Returns a map with keys in the form of PNR-authCode and values of {@link StationTransactionGroup} where each group is made up of 
	 * StationTransactions containing only card payments having the same PNR and same authCode. If multiple card payments are found on the 
	 * same StationTransaction, then the first authCode is selected.
	 * 
	 * The assumption here is that many station transactions belonging to the same PNR, having the same authorization code would
	 * pair with a *single* transaction on the payment gateway.
	 * 
	 * @return A Map of groups having the same PNR and authorization codes, under the same StationRecord.
	 */
	private Map<String, StationTransactionGroup> groupTransactions() {
		final Map<String, Map<String, List<StationTransaction>>> collect = stationTransactions
				.stream()
				.filter(tx -> tx.getCardPayments().size() > 0)
				.collect(Collectors.groupingBy(StationTransaction::getPNR,
						Collectors.groupingBy(tx -> {
					return tx.getCardPayments().get(0).getAuthCode();
				})));
		
		Map<String, StationTransactionGroup> groups = new HashMap<>();
		
		collect.forEach((pnr, authCodes) -> {
			authCodes.forEach((authCode, transactions) -> {
				groups.put(String.format("%s-%s", pnr, authCode), new StationTransactionGroup(pnr, authCode, transactions));
			});
		});
		
		return groups;
	}
	
	/**
	 * Selects the {@link StationTransactionGroup} from the {@link StationRecord} matching a particular PNR and authCode
	 * 
	 * @param pnr
	 * @param authCode
	 * @return {@link StationTransactionGroup} 
	 */
	public StationTransactionGroup getTransactionGroup(String pnr, String authCode) {
		return stationTransactionGroups.get(String.format("%s-%s", pnr, authCode));
	}
	
	@Override
	public String toString() {
		String stationTxs = StringUtils.join(stationTransactions.stream().map(record -> record.toString()).collect(Collectors.toList()), ASRReport.NEW_LINE);

		String[] asrReportSegments = new String[] {
				stationHeader.toString(),
				stationTxs,
				stationTotals.toString(),
				stationTotalsPerCurrency.toString()
		};
		
		return StringUtils.join(asrReportSegments, ASRReport.NEW_LINE);
	}
	
	public static class Builder {
		
		private StationRecord record;
		private StationTransaction.Builder transactionBuilder;
		
		public Builder(ASRReport parentReport) {
			record = new StationRecord(parentReport);
		}
		
		public Builder processLine(String line) {
	        final ASRRecordType recordType = ASRRecordType.fromString(line);
            
            switch (recordType) {
				case BOH03: {
					BOH03 boh03 = BOH03.fromString(line);
					record.stationHeader = boh03;
					return this;
				}
				case BOT94: {
					BOT94 bot94 = BOT94.fromString(line);
					record.stationTotalsPerCurrency = bot94;

					// This record type closes the current BKT. 
					// So, build the current BKT, if any, in preparation for an upcoming build() on this builder.
					StationTransaction transaction = transactionBuilder.build();
					record.stationTransactions.add(transaction);
					
					// We do not need the builder any more.
					transactionBuilder = null;
					return this;
				}
				case BOT93: {
					BOT93 bot93 = BOT93.fromString(line);
					record.stationTotals = bot93;
					
					return this;
				}
				case BKT06: {
					// We have found the start of a new transaction.
					// Close the previous and start afresh.

					if (transactionBuilder != null) {
						StationTransaction transaction = transactionBuilder.build();
						record.stationTransactions.add(transaction);
					}

					transactionBuilder = new StationTransaction.Builder(record);
					transactionBuilder.processLine(line);
					return this;
				}
				default: {
					// Let the inner BKT transaction builder handle the unknown line
					transactionBuilder.processLine(line);
					return this;
				}
			}

		}
		
		public StationRecord build() {
			record.stationTransactionGroups = record.groupTransactions();
			return record;
		}

	}

	public ASRReport getASRReport() {
		return asrReport;
	}
	
	public BOH03 getStationHeader() {
		return stationHeader;
	}
	
	public BOT93 getStationTotals() {
		return stationTotals;
	}
	
	public BOT94 getStationTotalsPerCurrency() {
		return stationTotalsPerCurrency;
	}
	
	public List<StationTransaction> getStationTransactions() {
		return stationTransactions;
	}
	
	public Map<String, StationTransactionGroup> getStationTransactionGroups() {
		return stationTransactionGroups;
	}
	
}
