package com.kmmaltairlines.asringester;

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

import com.kmmaltairlines.asringester.model.ASRReport;
import com.kmmaltairlines.asringester.model.StationTransaction;
import com.kmmaltairlines.asringester.model.bkp.BKP84;
import com.kmmaltairlines.asringester.payment.PaymentProcessor;
import com.kmmaltairlines.asringester.process.ReadFileStreamProcessor;
import com.kmmaltairlines.asringester.process.reporting.ASRBookingDetail;
import com.kmmaltairlines.asringester.process.reporting.ASRProcessReport;
import com.kmmaltairlines.asringester.utils.Utility;
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
						String paymentType = form.getFormOfPaymentType();
						String asrReportingState = null;
						String asrReportingErrorMessage = null;

						// route 1
						// se il tipo del pagamento inizia con "CC" o "XX", vuol dire che è stato trovato un metodo
						// di pagamento, quindi si procede con le modifiche. Altrimenti, resta invariato
						if (paymentType.startsWith("CC") || paymentType.startsWith("XX")) {
							log.info(
									"Encountered a card payment method, attempting lookup against PG_Payments to identify customer.");


							// Which PCC sold the ticket?
							try {
								// which set contains the PCC?
								if (this.pccContainer.isSabredx(pcc)) {
									log.info("Processing SabreDX Payment.");
									// lookup-sabredx-payments-subflow
									paymentProcessor.processPayment(trx, form);
									asrReportingState = paymentProcessor.getApcoProcessor().getAsrReportingState();
									asrReportingErrorMessage = paymentProcessor.getApcoProcessor().getAsrReportingErrorMessage();
								} else if (this.pccContainer.isWebsite(pcc)) {
									log.info("Processing Website Payment.");
									// lookup-website-payments-subflow
									paymentProcessor.processPayment(trx, form);
									asrReportingState = paymentProcessor.getApcoProcessor().getAsrReportingState();
									asrReportingErrorMessage = paymentProcessor.getApcoProcessor().getAsrReportingErrorMessage();
								} else if (this.pccContainer.isPaxport(pcc)) {
									// lookup-paxport-payments-processor
									paymentProcessor.getApcoProcessor().handleOGONEPayment(trx);
								} else if (this.pccContainer.isRyanair(pcc)){
									// lookup-ryanair-payments-processor
									paymentProcessor.getApcoProcessor().handleOGONEPayment(trx);
								} else {
									// INFO Unsupported PCC
									log.error(
											"Ignoring PCC since we do not support modifications on non-IBE and non-RyanAir payments.");
									paymentProcessor.getApcoProcessor().setAsrReportingState("UNCHANGED");
									asrReportingState = paymentProcessor.getApcoProcessor().getAsrReportingState();
								}

							} catch (Exception e) {
								log.error(
										"An error was encountered when attempting to lookup BIN information for CCPS payment for PNR: {}.",
										pnr);
								paymentProcessor.getApcoProcessor().setAsrReportingState("ERROR");
								paymentProcessor.getApcoProcessor().setAsrReportingErrorMessage(Utility.rootCause(e).toString());
								asrReportingState = paymentProcessor.getApcoProcessor().getAsrReportingState();
								asrReportingErrorMessage = paymentProcessor.getApcoProcessor().getAsrReportingErrorMessage();
								
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
							paymentProcessor.getApcoProcessor().setAsrReportingState("UNCHANGED");
							asrReportingState = paymentProcessor.getApcoProcessor().getAsrReportingState();
							paymentProcessor.getApcoProcessor().setAsrReportingErrorMessage(null);
							asrReportingErrorMessage = paymentProcessor.getApcoProcessor().getAsrReportingErrorMessage();
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

				// scrittura su file di uscita (file modificato), spostamento dalla cartella di lettura alla cartella di archivio (file invariato),
				// scrittura allegato per ogni processReport generato
				fstreamProcessor.writeOut(asrReport, filename);
				fstreamProcessor.moveInArchive(asrReport, filename);
				fstreamProcessor.writeReportForAttachment(processReport, filename);

				// ricerca dell'allegato specifico in base al filename
				List<File> attachmentFiles = new ArrayList<>();
				File attachment = fstreamProcessor.getAttachmentByFilename("ASRProcessReport_" + filename);

				if (attachment != null) {
					attachmentFiles.add(attachment);
				} else {
					log.warn("No attachment found for filename: {}", filename);
				}

				// invio email con report in allegato per ogni file processato
				mailRequest.setAttachments(attachmentFiles);
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
