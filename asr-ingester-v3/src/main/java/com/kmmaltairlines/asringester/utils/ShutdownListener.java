package com.kmmaltairlines.asringester.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

// per chiudere l'applicazione, inviare una richiesta POST all'endopoint:
// https://localhost:8443/actuator/shutdown

public class ShutdownListener {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
	 
    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        logger.info("ActiatorPocApplication shutdown");
    }
}
