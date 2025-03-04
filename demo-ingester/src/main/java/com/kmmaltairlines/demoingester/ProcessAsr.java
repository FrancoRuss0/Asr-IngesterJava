package com.kmmaltairlines.demoingester;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kmmaltairlines.demoingester.model.ASRReport;
import com.kmmaltairlines.demoingester.model.StationTransaction;
import com.kmmaltairlines.demoingester.model.StationTransactionGroup;
import com.kmmaltairlines.demoingester.model.bkp.BKP84;
import com.kmmaltairlines.demoingester.payment.PaymentProcessor;
import com.kmmaltairlines.demoingester.process.ReadFileStreamProcessor;
import com.kmmaltairlines.demoingester.process.reporting.ASRBookingDetail;
import com.kmmaltairlines.demoingester.process.reporting.ASRProcessReport;
import com.kmmaltairlines.demoingester.utils.Utility;
import com.kmmaltairlines.mail.EmailRequest;
import com.kmmaltairlines.mail.MailService;

@SpringBootApplication(scanBasePackages = "com")
@PropertySource("classpath:application.properties")
@EnableScheduling
public class ProcessAsr implements CommandLineRunner {

	private ReadFileStreamProcessor fstreamProcessor;
	private final PCCContainerFactoryBean pccContainer;
	private final ASRProcessReport processReport;
	private final MailService mailService;
	private final EmailRequest mailRequest;
	private boolean emailSent = false;

	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private PaymentProcessor paymentProcessor;

	public ProcessAsr(ReadFileStreamProcessor fstreamProcessor, PCCContainerFactoryBean pccContainer,
			ASRProcessReport processReport, MailService mailService, EmailRequest mailRequest) {
		this.fstreamProcessor = fstreamProcessor;
		this.pccContainer = pccContainer;
		this.processReport = processReport;
		this.mailService = mailService;
		this.mailRequest = mailRequest;
	}

	public static void main(String[] args) {
		SpringApplication.run(ProcessAsr.class, args);
	}

	public void run(String... args) throws Exception {
		processASRFile();
	}

	// process-asr-file-flow
	public void processASRFile() {

		List<ASRBookingDetail> bookingDetailsList = new ArrayList<>();

		try {
			List<ASRReport> reports = fstreamProcessor.process();


			// getAllStationTransactions da ASRReport
			for (ASRReport report : reports) {
				
				ASRProcessReport processReport = new ASRProcessReport();

				String filename = fstreamProcessor.getFilename();
				if (filename == null || filename.isEmpty()) {
					filename = "default_processed_filename"; // fallback
				}
				
				// creazione dell'ASRProcessReport con filename
				processReport.setFilename(filename);
				log.info("Filename -> {}", filename);
				List<StationTransaction> allTrxs = report.getAllStationTransactions();

				// for each station transaction
				for (StationTransaction trx : allTrxs) {
					List<BKP84> forms = trx.getFormOfPayments();

					// for each form of payment
					for (BKP84 form : forms) {

						// choice: is card payment?
						String pnr = trx.getPNR();
						String pcc = trx.getPCC();
						log.info("Processing transaction for {} payment/s for PNR: {}.", forms.size(), pnr);

						String seqNumber = form.getSeqNumber();
						String authCode = form.getAuthCode();
						String paymentType = form.getFormOfPaymentType();
						String asrReportingState = null;
						String asrReportingErrorMessage = null;

						// route 1
						if (paymentType.startsWith("CC") || paymentType.startsWith("XX")) {
							log.info(
									"Encountered a card payment method, attempting lookup against PG_Payments to identify customer.");

							// ref: alter-card-payment-method-flow
							// set transactionGroup per pnr e authcode
							StationTransactionGroup transactionGroup = trx.getStationRecord().getTransactionGroup(pnr,
									authCode);

							// Which PCC sold the ticket?
							try {
								// which set contains the PCC?
								if (this.pccContainer.isSabredx(pcc)) {
									log.info("Processing SabreDX Payment.");
									asrReportingState = paymentProcessor.getAsrReportingState();
									asrReportingState = "CHANGED";
									paymentProcessor.processPayment(trx, pcc, form);
								} else if (this.pccContainer.isWebsite(pcc)) {
									log.info("Processing Website Payment.");
									asrReportingState = paymentProcessor.getAsrReportingState();
									paymentProcessor.processPayment(trx, pcc, form);
								} else if (this.pccContainer.isPaxport(pcc) || this.pccContainer.isRyanair(pcc)) {
									paymentProcessor.handleOGONEPayment(trx);
								} else {
									log.error(
											"Ignoring PCC since we do not support modifications on non-IBE and non-RyanAir payments.");
									asrReportingState = "ERROR";
								}

								// simulazione errore
//								throw new RuntimeException("Simulated error during payment processing");

							} catch (Exception e) {
								log.error(
										"An error was encountered when attempting to lookup BIN information for CCPS payment for PNR: {}.",
										pnr);
								asrReportingState = "ERROR";
								asrReportingErrorMessage = Utility.rootCause(e).toString();

								// email di errore
								if (!emailSent) {
									emailSent = true;
									mailRequest.setTemplateMessage("asrFileFailure", null);
									mailRequest.setSubject("Error in ASR Report");
									mailService.sendEmail(mailRequest);
								}
							}
						}
						// route default
						else {
							log.info("Encountered a payment method. Leaving value untouched.");
							asrReportingState = "UNCHANGED";
						}

						// invoke saleDateAsLocalDate()
						LocalDate locSaleDate = trx.getSaleDateAsLocalDate();

						ASRBookingDetail bookingDetails = new ASRBookingDetail(seqNumber, pnr, locSaleDate, pcc,
								paymentType, asrReportingState, asrReportingErrorMessage);

						// aggiunta di dettagli all'ASRProcessReport
						processReport.addBookingDetail(bookingDetails);
						bookingDetailsList = processReport.getBookingDetails();

						// setProcessEnd
						processReport.setProcessEnd(ZonedDateTime.now());

					}
				}

				fstreamProcessor.writeReportForAttachment(processReport, filename);
			}

			// allegati
			mailRequest.setAttachments(fstreamProcessor.getAttachments());

			// email
	        if (!emailSent) {
	            try {
	                log.info("Preparing ASR Process Report");
	                mailRequest.setSubject("ASR Report - Process Start: " + processReport.getProcessStartAsString());
	                mailRequest.setTemplateMessage("asrFileSuccess", null);
	                mailService.sendEmail(mailRequest);  // Invio email di successo
	                log.info("ASR Process Report successfully sent via email.");
	            } catch (Exception e) {
	                log.error("An error was encountered while trying to send the ASR Process Report via email.", e);
	            }
	        }
		} catch (Exception e) {
			log.info("General error occurred during ASR file processing.");
			e.printStackTrace();
			mailRequest.setTemplateMessage("asrFileFailure", null);
			mailRequest.setSubject("Error in ASR Report");
			mailRequest.setAttachments(fstreamProcessor.getAttachments());
			mailService.sendEmail(mailRequest);
		}
	}
}
