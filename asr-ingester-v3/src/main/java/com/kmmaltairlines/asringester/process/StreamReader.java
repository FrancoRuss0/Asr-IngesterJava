package com.kmmaltairlines.asringester.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.kmmaltairlines.asringester.exceptions.CouldNotParseASRFileException;
import com.kmmaltairlines.asringester.model.ASRReport;

@Component
public class StreamReader {
	
	private String filename;
	Logger log = LoggerFactory.getLogger(getClass());
	
	public ASRReport processASRFile(File asrFile) throws IOException {
		if (asrFile == null || !asrFile.exists() || !asrFile.isFile()) {
			log.error("An error was encountered while trying to read the ASR file.");
			throw new IllegalArgumentException("Invalid file: " + asrFile);
		} else {
			this.filename = asrFile.getName();
			log.info("Received {} ASR file.", filename);
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(asrFile))) {
			// Builder() restituisce una nuova istanza ad ogni chiamata
			ASRReport.Builder builder = new ASRReport.Builder();

			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				builder.processLine(currentLine);
			}

			return builder.build();
		} catch (IOException e) {
			throw new CouldNotParseASRFileException("Could not parse ASR file: " + asrFile.getName(), e);
		}
	}
	
	public String getFilename() {
		return filename;
	}
}
