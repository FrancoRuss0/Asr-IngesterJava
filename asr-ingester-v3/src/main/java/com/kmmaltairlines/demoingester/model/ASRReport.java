package com.kmmaltairlines.demoingester.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.kmmaltairlines.demoingester.model.bft.BFT99;

@Component
public class ASRReport {
	
	protected static final String NEW_LINE = "\n";
	
	// BFH/01 line
	private BFH01 fileHeader;
	// BCH/02 line
	private BCH02 processingHeader;
	
	// BOH/03 lines and subsequent lines
	private List<StationRecord> stationRecords = new ArrayList<>();

	// BFT/99 lines. This record marks the end of the file.
	private BFT99 totalsPerCurrencyType;

	public List<StationTransaction> getAllStationTransactions() {
		List<StationTransaction> allTxs = stationRecords.stream()
													.map(record -> record.getStationTransactions())
													.flatMap(e -> e.stream())
													.collect(Collectors.toList());
		
		return allTxs;
	}

	public BFH01 getFileHeader() {
		return fileHeader;
	}
	
	public BCH02 getProcessingHeader() {
		return processingHeader;
	}
	
	public List<StationRecord> getStationRecords() {
		return stationRecords;
	}
	
	public BFT99 getTotalsPerCurrencyType() {
		return totalsPerCurrencyType;
	}
	
	@Override
	public String toString() {	
		StringBuilder builder = new StringBuilder();
		
		builder.append(fileHeader.toString());
		builder.append(NEW_LINE);
		builder.append(processingHeader.toString());
		builder.append(NEW_LINE);
		if (stationRecords.size() > 0) {
			String stations = StringUtils.join(stationRecords.stream().map(record -> record.toString()).collect(Collectors.toList()), NEW_LINE);
			builder.append(stations);
			builder.append(NEW_LINE);
		}
		builder.append(totalsPerCurrencyType.toString());
		builder.append(NEW_LINE); // Apparently the new-line at the end of the file is required...
		
		return builder.toString();
	}

	
	public static class Builder {
		
		private ASRReport report;
		private StationRecord.Builder stationRecordBuilder;
		
		public Builder() {
			report = new ASRReport();
		}
		
		public Builder processLine(String line) {
		    
			// Just ignore empty lines.
			if (StringUtils.isBlank(line)) {
				return this;
			}
			
		    final ASRRecordType recordType = ASRRecordType.fromString(line);
            
            switch (recordType) {
				case BFH01: {
					BFH01 fileHeader = BFH01.fromString(line);
					report.fileHeader = fileHeader;
					return this;
				}
				case BCH02: {
					BCH02 processingHeader = BCH02.fromString(line);
					report.processingHeader = processingHeader;
					return this;
				}
				case BFT99: {
					BFT99 totalsPerCurrencyType = BFT99.fromString(line);
					report.totalsPerCurrencyType = totalsPerCurrencyType;

					// This record type closes the current BOH. 
					// So, build the current BOH, in preparation for the end of file,
					// unless the file did not have any station records.

					if (stationRecordBuilder != null) {
						StationRecord lastStationRecord = stationRecordBuilder.build();
						report.stationRecords.add(lastStationRecord);
						
						stationRecordBuilder = null;
					}
					return this;
				}
				case BOH03: {
					// We found a new BOH. Close the existing one, if any, and start afresh with the new.
					
					if (stationRecordBuilder != null) {
						StationRecord newStationRecord = stationRecordBuilder.build();
						report.stationRecords.add(newStationRecord);
					}

					stationRecordBuilder = new StationRecord.Builder(report);
					stationRecordBuilder.processLine(line);
					return this;
				}
				default: {
					// Let the BOH station record builder handle the unknown line.
					stationRecordBuilder.processLine(line);
					return this;
				}
			}
			
		}

		public ASRReport build() {
			return this.report;
		}
	}
	
}
