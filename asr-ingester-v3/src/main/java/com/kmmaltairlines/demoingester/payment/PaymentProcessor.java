package com.kmmaltairlines.demoingester.payment;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kmmaltairlines.demoingester.model.StationTransaction;
import com.kmmaltairlines.demoingester.model.bkp.BKP84;
import com.kmmaltairlines.demoingester.process.AirportToCountryCodeReader;

@Service
public class PaymentProcessor {

	Logger log = LoggerFactory.getLogger(getClass());
	
	private final APCOPaymentProcessor apcoProcessor;
	
	public PaymentProcessor (APCOPaymentProcessor apcoProcessor) {
		this.apcoProcessor = apcoProcessor;
	}
	
	public void processPayment(StationTransaction stationTransaction, BKP84 form) {
		if(this.apcoProcessor != null) {
			this.apcoProcessor.processAPCOPayment(stationTransaction, form);
		} else {
			throw new IllegalStateException("APCOPaymentProcessor is not initialized.");
		}
		
		// set departureAirport variable
		String departureAirport = stationTransaction.getPointOfDeparture();
		
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
		
		// choice
		if (departureCountry == null) {
			log.error("Error: Country for departure airport {} not found.", departureAirport);
			return;
		} else if (departureCountry.equals("LM") || departureCountry.equals("LS")) {
			// ref: APCO
			apcoProcessor.processAPCOPayment(stationTransaction, form);
		} else {
			// ref: OGONE
			handleOGONEPayment(stationTransaction);
		}
	}
	
	// TODO: implementare per i pagamenti OGONE (la logica potrebbe essere la stessa di APCO)
	public void handleOGONEPayment(StationTransaction trx) {
		log.info("Handling OGONE Payment for PNR: {}.", trx.getPNR());
	}

	public APCOPaymentProcessor getApcoProcessor() {
		return apcoProcessor;
	}
	
}
