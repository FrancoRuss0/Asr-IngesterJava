package com.kmmaltairlines.demoingester.process;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class AirportToCountryCodeReader {
	
	private static final String CODES_CSV = "AirportCodes.txt";
	
	public static Map<String, String> loadAirportToCountryCodes() throws IOException {
		Map<String, String> airportToCountryCodes = new HashMap<>();

		Reader in = new InputStreamReader(AirportToCountryCodeReader.class.getClassLoader().getResourceAsStream(CODES_CSV));
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
		
		for (CSVRecord record : records) {
		    String airportCode = record.get(0);
		    // Only load IATA Codes
		    if (airportCode.length() == 3) {
		    	airportToCountryCodes.put(airportCode, record.get(3));
		    }
		}
		
		return airportToCountryCodes;
	}
}
