package com.kmmaltairlines.demoingester.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.kmmaltairlines.demoingester.exceptions.UnknownRecordTypeException;
import com.kmmaltairlines.demoingester.model.bar.BAR64;
import com.kmmaltairlines.demoingester.model.bar.BAR65;
import com.kmmaltairlines.demoingester.model.bkf.BKF81;
import com.kmmaltairlines.demoingester.model.bki.BKI63;
import com.kmmaltairlines.demoingester.model.bkp.BKP84;
import com.kmmaltairlines.demoingester.model.bkp.BKP85;
import com.kmmaltairlines.demoingester.model.bkp.BKP86;
import com.kmmaltairlines.demoingester.model.bkp.BKP88;
import com.kmmaltairlines.demoingester.model.bks.BKS24;
import com.kmmaltairlines.demoingester.model.bks.BKS30;
import com.kmmaltairlines.demoingester.model.bks.BKS31;
import com.kmmaltairlines.demoingester.model.bks.BKS32;
import com.kmmaltairlines.demoingester.model.bks.BKS39;
import com.kmmaltairlines.demoingester.model.bks.BKS45;
import com.kmmaltairlines.demoingester.model.bks.BKS46;
import com.kmmaltairlines.demoingester.model.bks.BKS47;
import com.kmmaltairlines.demoingester.model.bks.BKS48;
import com.kmmaltairlines.demoingester.model.bmd.BMD75;
import com.kmmaltairlines.demoingester.model.bmd.BMD76;
import com.kmmaltairlines.demoingester.model.bmp.BMP70;

public class StationTransaction {
	// BKT/06 line
	private BKT06 transactionHeader;

	// BKS/24 lines. One is always present and acts as the master document identification 
	// line (usually the first BKS/24). The rest, if any, will denote conjunction tickets.
	private BKS24 masterDocumentIdentification;
	private List<BKS24> documentIdentification = new ArrayList<>();
	// BKS/30 lines
	private List<BKS30> documentAmountsBKS = new ArrayList<>();
	// BKS/31 lines
	private List<BKS31> exchangeDocumentAmounts = new ArrayList<>();
	// BKS/32 lines
	private List<BKS32> exchangeDocumentTaxAmounts = new ArrayList<>();
	// BKS/39 lines
	private List<BKS39> commissions = new ArrayList<>();
	// BKS/45 lines
	private List<BKS45> relationDocumentInformation = new ArrayList<>();
	// BKS/46 lines
	private List<BKS46> msrEndorsements = new ArrayList<>();
	// BKS/47 lines
	private List<BKS47> extendedRemarks = new ArrayList<>();
	// BKS/48 lines
	private List<BKS48> variableRecords = new ArrayList<>(); // NOT USED

	// BKI/63 lines. This will also include segments that appear after BKS/24 conjunction lines.
	private List<BKI63> itinerarySegments = new ArrayList<>();

	// BAR/64 lines
	private List<BAR64> documentAmountsBAR = new ArrayList<>();
	// BAR/65 lines
	private List<BAR65> passengerInformation = new ArrayList<>();

	// BMP/70 lines
	private List<BMP70> miscDocumentInformation = new ArrayList<>();

	// BKF/81 lines
	private List<BKF81> fareCalculations = new ArrayList<>();

	// BMD/75 lines
	private List<BMD75> EMDCouponDetailRecord = new ArrayList<>();

	// BKP/84 lines
	private List<BKP84> formOfPayments = new ArrayList<>();
	// BKP/85 lines
	private List<BKP85> exchangeDocuments = new ArrayList<>();

	// BKP/86 lines
	// TODO: WHAT IS THIS????
	private List<BKP86> UNKNOWN = new ArrayList<>();
	
	// BKP/88 lines
	private List<BKP88> BKP88_UNKNOWN = new ArrayList<>();
	
	// BMD/76 lines
	// TODO: WHAT IS THIS????
	private List<BMD76> BMD76_UNKNOWN = new ArrayList<>();

	private StationRecord stationRecord;
	
	public StationTransaction (StationRecord parentRecord) {
		this.stationRecord = parentRecord;
	}
	
	public StationRecord getStationRecord() {
		return stationRecord;
	}
	
	public String getPNR() {
		return masterDocumentIdentification.getPNR();
	}
	
	public java.sql.Date getSaleDateAsSQLDate() {
		return java.sql.Date.valueOf(transactionHeader.getSaleDate());
	}
	
	public LocalDate getSaleDateAsLocalDate() {
		return transactionHeader.getSaleDate();
	}
	
	public String getPCC() {
		return stationRecord.getStationHeader().getPCC();
	}
	
	/**
	 * Retrieves the master document identification, that is, the BKS/24 line that does not have "CNJ" within the  
	 * conjunction ticket indicator section of the line. There should only be one (usually the first BKS/24 line).
	 * 
	 * @return {@link BKS24} representing the master document identification line
	 */
	private BKS24 extractMasterDocumentIdentification() {
		return documentIdentification.stream().filter(d -> !d.isConjunction()).findFirst().get();
	}
	
	public String getPointOfDeparture() {
		if (itinerarySegments.isEmpty()) {
			// TODO: What do we do here?
			throw new RuntimeException("Cannot determine country / airport of origin.");
		}
		
		BKI63 firstSegment = itinerarySegments.get(0);
		String originAirport = firstSegment.getOriginAirport();
		return originAirport;
	}
	
	public List<BKP84> getCardPayments() {
		return this.formOfPayments.stream().filter(p -> p.isCardPayment()).collect(Collectors.toList());
	}

	public BigDecimal getCardPaymentsTotal() {
		return formOfPayments.stream().filter(p -> p.isCardPayment()).map(p -> p.getTrxAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	public BKT06 getTransactionHeader() {
		return transactionHeader;
	}

	public BKS24 getMasterDocumentIdentification() {
		return masterDocumentIdentification;
	}

	public List<BKS24> getDocumentIdentification() {
		return documentIdentification;
	}

	public List<BKS30> getDocumentAmountsBKS() {
		return documentAmountsBKS;
	}

	public List<BKS31> getExchangeDocumentAmounts() {
		return exchangeDocumentAmounts;
	}

	public List<BKS32> getExchangeDocumentTaxAmounts() {
		return exchangeDocumentTaxAmounts;
	}

	public List<BKS39> getCommissions() {
		return commissions;
	}

	public List<BKS45> getRelationDocumentInformation() {
		return relationDocumentInformation;
	}

	public List<BKS46> getMsrEndorsements() {
		return msrEndorsements;
	}

	public List<BKS47> getExtendedRemarks() {
		return extendedRemarks;
	}

	public List<BKS48> getVariableRecords() {
		return variableRecords;
	}

	public List<BKI63> getItinerarySegments() {
		return itinerarySegments;
	}

	public List<BAR64> getDocumentAmountsBAR() {
		return documentAmountsBAR;
	}

	public List<BAR65> getPassengerInformation() {
		return passengerInformation;
	}

	public List<BMP70> getMiscDocumentInformation() {
		return miscDocumentInformation;
	}

	public List<BKF81> getFareCalculations() {
		return fareCalculations;
	}

	public List<BMD75> getEMDCouponDetailRecord() {
		return EMDCouponDetailRecord;
	}

	public List<BKP84> getFormOfPayments() {
		return formOfPayments;
	}

	public List<BKP85> getExchangeDocuments() {
		return exchangeDocuments;
	}

	public List<BKP86> getUNKNOWN() {
		return UNKNOWN;
	}

	public List<BKP88> getBKP88_UNKNOWN() {
		return BKP88_UNKNOWN;
	}
	
	public List<BMD76> getBMD76_UNKNOWN() {
        return BMD76_UNKNOWN;
    }

    @Override
	public String toString() {
		final List<ASRRecord> stationTxRecords = new ArrayList<>();
		
		stationTxRecords.add(transactionHeader);
		stationTxRecords.addAll(documentIdentification);
		stationTxRecords.addAll(documentAmountsBKS);
		stationTxRecords.addAll(exchangeDocumentAmounts);
		stationTxRecords.addAll(exchangeDocumentTaxAmounts);
		stationTxRecords.addAll(commissions);
		stationTxRecords.addAll(relationDocumentInformation);
		stationTxRecords.addAll(msrEndorsements);
		stationTxRecords.addAll(extendedRemarks);
		stationTxRecords.addAll(variableRecords);
		stationTxRecords.addAll(itinerarySegments);
		stationTxRecords.addAll(documentAmountsBAR);
		stationTxRecords.addAll(passengerInformation);
		stationTxRecords.addAll(miscDocumentInformation);
		stationTxRecords.addAll(EMDCouponDetailRecord);
		stationTxRecords.addAll(BMD76_UNKNOWN);
		stationTxRecords.addAll(fareCalculations);
		stationTxRecords.addAll(formOfPayments);
		stationTxRecords.addAll(exchangeDocuments);
		stationTxRecords.addAll(UNKNOWN);
		
		// A sort approach is used here since BKS/24 and BKI/63 lines cannot be printed in sequential order 
		// since the list can contain lines that appear "later" in the file due to a conjunction ticket entry.
		// Hence we first collect all the entries in a list and sort this based on the line sequence number.
		stationTxRecords.sort((r1, r2) -> r1.getSeqNumber().compareTo(r2.getSeqNumber()));
		
		return StringUtils.join(stationTxRecords, ASRReport.NEW_LINE);
		
	}
	
	public static class Builder {

		private final StationTransaction transaction;
		
		public Builder(final StationRecord parentRecord) {
			transaction = new StationTransaction(parentRecord);
		}
		
		public Builder processLine(final String line) {
			final ASRRecordType recordType = ASRRecordType.fromString(line);
			
			switch (recordType) {
				case BKT06: {
					transaction.transactionHeader = BKT06.fromString(line);
					return this;
				}
				case BKS24: {
					// This will include entries for conjunction tickets.
					transaction.documentIdentification.add(BKS24.fromString(line));
					return this;
				}
				case BKS30: {
					transaction.documentAmountsBKS.add(BKS30.fromString(line));
					return this;
				}
				case BKS31: {
					transaction.exchangeDocumentAmounts.add(BKS31.fromString(line));
					return this;
				}
				case BKS32: {
					transaction.exchangeDocumentTaxAmounts.add(BKS32.fromString(line));
					return this;
				}
				case BKS39: {
					transaction.commissions.add(BKS39.fromString(line));
					return this;
				}
				case BKS45: {
					transaction.relationDocumentInformation.add(BKS45.fromString(line));
					return this;
				}
				case BKS46: {
					transaction.msrEndorsements.add(BKS46.fromString(line));
					return this;
				}
                case BKS47: {
                    transaction.extendedRemarks.add(BKS47.fromString(line));
                    return this;
                }
				case BKS48: {
					transaction.variableRecords.add(BKS48.fromString(line));
					return this;
				}
				case BKI63: {
					// This will include segments that are relative to conjunction tickets.
					transaction.itinerarySegments.add(BKI63.fromString(line));
					return this;
				}
				case BAR64: {
					transaction.documentAmountsBAR.add(BAR64.fromString(line));
					return this;
				}
				case BAR65: {
					transaction.passengerInformation.add(BAR65.fromString(line));
					return this;
				}
				case BMP70: {
					transaction.miscDocumentInformation.add(BMP70.fromString(line));
					return this;
				}
				case BKF81: {
					transaction.fareCalculations.add(BKF81.fromString(line));
					return this;
				}
				case BMD75: {
					transaction.EMDCouponDetailRecord.add(BMD75.fromString(line));
					return this;
				}
				case BKP84: {
					transaction.formOfPayments.add(BKP84.fromString(line));
					return this;
				}
				case BKP85: {
					transaction.exchangeDocuments.add(BKP85.fromString(line));
					return this;
				}
				case BKP86: {
					transaction.UNKNOWN.add(BKP86.fromString(line));
					return this;
				}
				case BKP88: {
					transaction.BKP88_UNKNOWN.add(BKP88.fromString(line));
					return this;
				}
				
				case BMD76: {
				    transaction.BMD76_UNKNOWN.add(BMD76.fromString(line));
				    return this;
				}
				default: {
					throw new UnknownRecordTypeException(line);
				}
			}
		}
		
		public StationTransaction build() {
			transaction.masterDocumentIdentification = transaction.extractMasterDocumentIdentification();
			return transaction;
		}
		
	}
	
}
