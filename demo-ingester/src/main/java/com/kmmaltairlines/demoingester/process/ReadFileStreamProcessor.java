package com.kmmaltairlines.demoingester.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kmmaltairlines.demoingester.config.AsrFileConfig;
import com.kmmaltairlines.demoingester.file.ASRFileNameFilter;
import com.kmmaltairlines.demoingester.model.ASRReport;
import com.kmmaltairlines.demoingester.payment.APCOPaymentProcessor;
import com.kmmaltairlines.demoingester.process.reporting.ASRProcessReport;
import com.kmmaltairlines.demoingester.process.reporting.ASRReportingUtils;

@Service
public class ReadFileStreamProcessor {
	Logger log = LoggerFactory.getLogger(getClass());

	private AsrFileConfig fileConfig;
	private StreamReader streamReader;
	private Set<String> processedFilenames = new HashSet<>();
	private APCOPaymentProcessor paymentProcessor;
	private ASRFileNameFilter nameFilter;

	public ReadFileStreamProcessor(AsrFileConfig fileConfig, StreamReader streamReader,
			APCOPaymentProcessor paymentProcessor, ASRFileNameFilter nameFilter) {
		this.fileConfig = fileConfig;
		this.streamReader = streamReader;
		this.paymentProcessor = paymentProcessor;
		this.nameFilter = nameFilter;
	}

	public Map<String, ASRReport> process() {
		File directoryPath = new File(fileConfig.getPathIn());

		// vengono processati solo i file il cui nome rispetta quello specificato in ASRFileNameFilter
		// e tali che siano "vecchi" di almeno delayInDays giorni. Spring gestisce in automatico delayInDays
		// con valore pari a zero perché è di tipo in
		File[] fileList = directoryPath.listFiles(nameFilter);

		if (fileList == null || fileList.length == 0) {
			return new HashMap<>();
		}

		Map<String, ASRReport> reports = new HashMap<>();
		for (File file : fileList) {

			String currentFilename = file.getName();

			if (processedFilenames.contains(file.getName())) {
				continue;
			}

			try {
				ASRReport asrReport = streamReader.processASRFile(file);
				reports.put(currentFilename, asrReport);
				processedFilenames.add(currentFilename);

			} catch (Exception e) {
				log.error("An error was encountered while trying to read the ASR file: {}.", e.getMessage());
			}
		}

		return reports;
	}

	// common method to write on files
	private void writeToFile(String path, String content) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
			writer.write(content);
			writer.newLine();
		} catch (IOException e) {
			log.error("An error was encountered while trying to save the ASR file: {}.", e.getMessage());
		}
	}

	// writing files in out folder (asr_ingester2)
	public void writeOut(ASRReport report, String filename) {
		String outputPath = fileConfig.getPathOut() + "/" + filename;

		report.getAllStationTransactions().forEach(
				trx -> trx.getFormOfPayments().forEach(form -> paymentProcessor.processAPCOPayment(trx, form)));
		writeToFile(outputPath, report.toString());

		log.info("Finished processing {} ASR file and sent to File out.", filename);
	}

	public void moveInArchive(ASRReport report, String filename) {
		Path archivePath = Paths.get(fileConfig.getPathArchive(), filename);
		Path sourcePath = Paths.get(fileConfig.getPathIn(), filename);

		writeToFile(archivePath.toString(), report.toString());

		try {
			Files.move(sourcePath, archivePath, StandardCopyOption.REPLACE_EXISTING);
			log.info("File moved to archive: {}.", archivePath);
		} catch (IOException e) {
			log.error("Failed to move file {} to archive: {}.", filename, e.getMessage());
		}

		log.info("ASR file {} successfully saved in archive.", filename);
	}

	// writing files for attachment in folder (asr_ingester2/attachments)
	public void writeReportForAttachment(ASRProcessReport processReport, String filename) {
		log.info("Starting to write attachments...");

		String attachPath = fileConfig.getPathAttachments() + "/" + filename;

		String reportContent = ASRReportingUtils.createASRProcessReport(processReport);

		// rimozione righe con details duplicati e State = null
		String[] lines = reportContent.split("\n");
		Set<String> uniqueEntries = new HashSet<>();
		StringBuilder filteredReportContent = new StringBuilder();
		filteredReportContent.append(lines[0]).append("\n").append(lines[1]).append("\n").append(lines[2]).append("\n");

		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];

			if (!hasNullState(line)) {
				String uniqueKey = extractUniqueKey(line);
				if (uniqueKey != null && uniqueEntries.add(uniqueKey)) {
					filteredReportContent.append(line).append("\n");
				}
			}
		}

		writeToFile(attachPath, filteredReportContent.toString());
		log.info("Finished processing {} and created an attachment.", filename);
	}

	private String extractUniqueKey(String line) {
		try {
			// estrazione dei campi rilevanti (PNR, SaleDate, PCC, PaymentMethod) per
			// evitare duplicati
			String[] parts = line.split(", ");
			String pnr = null, saleDate = null, pcc = null, paymentMethod = null;

			for (String part : parts) {
				if (part.startsWith("PNR: "))
					pnr = part.substring(5);
				else if (part.startsWith("SaleDate: "))
					saleDate = part.substring(10);
				else if (part.startsWith("PCC: "))
					pcc = part.substring(5);
				else if (part.startsWith("PaymentMethod: "))
					paymentMethod = part.substring(15);
			}

			if (pnr != null && saleDate != null && pcc != null && paymentMethod != null) {
				return pnr + "|" + saleDate + "|" + pcc + "|" + paymentMethod;
			}
		} catch (Exception e) {
			log.error("Error extracting unique key from line: {}", line, e);
		}
		return null;
	}

	private boolean hasNullState(String line) {
		try {
			String[] parts = line.split(", ");
			for (String part : parts) {
				if (part.startsWith("State: ") && part.substring(7).trim().equalsIgnoreCase("null")) {
					return true; // Escludi la riga se State è "null"
				}
			}
		} catch (Exception e) {
			log.error("Error checking State in line: {}", line, e);
		}
		return false;
	}

	public List<File> getAttachments() {
		List<File> attachments = new ArrayList<>();
		File attachFolder = new File(fileConfig.getPathAttachments());

		File[] files = attachFolder.listFiles(file -> file.isFile());

		if (files != null) {
			for (File file : files) {
				attachments.add(file);
			}
		} else {
			log.error("Error: attachments not found.");
		}
		return attachments;
	}

	public File getAttachmentByFilename(String filename) {
		File attachFolder = new File(fileConfig.getPathAttachments());

		if (!attachFolder.exists() || !attachFolder.isDirectory()) {
			log.error("Error: attachments folder not found.");
			return null;
		}

		File[] files = attachFolder.listFiles(file -> file.isFile() && file.getName().equals(filename));

		if (files != null && files.length > 0) {
			return files[0]; // Restituisce il file corrispondente al filename
		} else {
			log.warn("No attachment found for filename: {}", filename);
			return null;
		}
	}

	public Set<String> getProcessedFileNames() {
		return new HashSet<>(processedFilenames);
	}
}
