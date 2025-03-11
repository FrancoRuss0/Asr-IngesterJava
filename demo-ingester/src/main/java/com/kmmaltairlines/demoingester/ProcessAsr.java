package com.kmmaltairlines.demoingester;

import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

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
public class ProcessAsr {

	private PaymentProcessor paymentProcessor;
	private ReadFileStreamProcessor fstreamProcessor;
	private final PCCContainerFactoryBean pccContainer;
	private final MailService mailService;
	private final EmailRequest mailRequest;
	private boolean emailSent = false;

	Logger log = LoggerFactory.getLogger(getClass());

	public ProcessAsr(PaymentProcessor paymentProcessor, ReadFileStreamProcessor fstreamProcessor,
			PCCContainerFactoryBean pccContainer, MailService mailService, EmailRequest mailRequest) {
		this.fstreamProcessor = fstreamProcessor;
		this.pccContainer = pccContainer;
		this.mailService = mailService;
		this.mailRequest = mailRequest;
		this.paymentProcessor = paymentProcessor;
	}

	public static void main(String[] args) {
		SpringApplication.run(ProcessAsr.class, args);
	}

	// process-asr-file-flow
	@Scheduled(fixedRateString = "#{@asrFileConfig.pollingFrequency}")
	public void processASRFile() {

		log.info("Scanning for new files...");

		try {
			Map<String, ASRReport> reports = fstreamProcessor.process();

			if (reports.isEmpty()) {
				log.error("No files found.");
			} else {
				log.info("Processed {} new ASR report/s.", reports.size());
			}

			// getAllStationTransactions da ASRReport
			for (Map.Entry<String, ASRReport> entry : reports.entrySet()) {

				String filename = entry.getKey();
				ASRReport asrReport = entry.getValue();

				ASRProcessReport processReport = new ASRProcessReport();

				if (filename == null || filename.isEmpty()) {
					filename = "default_processed_filename"; // fallback
				}

				// creazione dell'ASRProcessReport con filename
				processReport.setFilename(filename);
				log.info("Filename -> {}", filename);
				List<StationTransaction> allTrxs = asrReport.getAllStationTransactions();

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
							// set transactionGroup per pnr e authcode. Inutilizzata ???
							StationTransactionGroup transactionGroup = trx.getStationRecord().getTransactionGroup(pnr,
									authCode);

							// Which PCC sold the ticket?
							try {
								// which set contains the PCC?
								if (this.pccContainer.isSabredx(pcc)) {
									log.info("Processing SabreDX Payment.");
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
								paymentProcessor.setAsrReportingErrorMessage(asrReportingErrorMessage);

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

						// setProcessEnd
						processReport.setProcessEnd(ZonedDateTime.now());

					}
				}

				fstreamProcessor.writeOut(asrReport, filename);
				fstreamProcessor.moveInArchive(asrReport, filename);
				fstreamProcessor.writeReportForAttachment(processReport, filename);

				// ricerca dell'allegato specifico in base al filename
				List<File> attachmentFiles = new ArrayList<>();
				File attachment = fstreamProcessor.getAttachmentByFilename(filename);

				if (attachment != null) {
					attachmentFiles.add(attachment);
				} else {
					log.warn("No attachment found for filename: {}", filename);
				}

				mailRequest.setAttachments(attachmentFiles);
				// email
				if (!emailSent) {
					try {
						log.info("Preparing ASR Process Report");
						mailRequest
								.setSubject("ASR Report - Process Start: " + processReport.getProcessStartAsString());
						mailRequest.setTemplateMessage("asrFileSuccess", null);
						mailService.sendEmail(mailRequest); // Invio email di successo
						log.info("ASR Process Report successfully sent via email.");
					} catch (Exception e) {
						log.error("An error was encountered while trying to send the ASR Process Report via email.", e);
					}
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
