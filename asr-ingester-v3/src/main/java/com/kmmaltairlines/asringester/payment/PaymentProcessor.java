package com.kmmaltairlines.asringester.payment;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kmmaltairlines.asringester.model.StationTransaction;
import com.kmmaltairlines.asringester.model.bkp.BKP84;
import com.kmmaltairlines.asringester.process.AirportToCountryCodeReader;

@Service
public class PaymentProcessor {

	Logger log = LoggerFactory.getLogger(getClass());
	
	private final LookupPayments apcoProcessor;
	
	public PaymentProcessor (LookupPayments apcoProcessor) {
		this.apcoProcessor = apcoProcessor;
	}
	
	public void processPayment(StationTransaction stationTransaction, BKP84 form) {
		if(this.apcoProcessor != null) {
			this.apcoProcessor.processApcoPayment(stationTransaction, form);
		} else {
			throw new IllegalStateException("APCOPaymentProcessor is not initialized.");
		}
		
		// set departureAirport variable
		String departureAirport = stationTransaction.getPointOfDeparture();
		
		// viene recuperato l'aeroporto d'origine (pointOfDeparture) dalla transazione per controllare che corrisponda ad uno degli aeroporti
		// presenti nel file "AirportCodes.txt"
		// invoke loadAirportToCountryCodes
		Map<String, String> airportToCountryCodes = null;
		try {
			airportToCountryCodes = AirportToCountryCodeReader.loadAirportToCountryCodes();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// set departureCountry variable
		String departureCountry = airportToCountryCodes.get(departureAirport);
		
		// choice -> se l'aeroporto di partenza esiste viene effettuato un controllo sul provider della transazione (APCO o OGONE)
		if (departureCountry == null) {
			log.error("Error: Country for departure airport {} not found.", departureAirport);
			return;
		} else if (departureCountry.equals("LM") || departureCountry.equals("LS")) {
			// ref: APCO
			apcoProcessor.processApcoPayment(stationTransaction, form);
		} else {
			// ref: OGONE -> da implementare
			apcoProcessor.handleOGONEPayment(stationTransaction);
		}
	}

	public LookupPayments getApcoProcessor() {
		return apcoProcessor;
	}
	
}
