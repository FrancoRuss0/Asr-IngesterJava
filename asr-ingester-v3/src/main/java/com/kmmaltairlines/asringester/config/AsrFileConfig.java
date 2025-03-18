package com.kmmaltairlines.asringester.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class AsrFileConfig {

	private String pathIn;
	private String pathOut;
	private String pathArchive;
	private String pathAttachments;
	private long pollingFrequency;
	
	public String getPathIn() {
		return pathIn;
	}

	public void setPathIn(String pathIn) {
		this.pathIn = pathIn;
	}

	public String getPathOut() {
		return pathOut;
	}

	public void setPathOut(String pathOut) {
		this.pathOut = pathOut;
	}

	public String getPathArchive() {
		return pathArchive;
	}

	public void setPathArchive(String pathArchive) {
		this.pathArchive = pathArchive;
	}

	public long getPollingFrequency() {
		return pollingFrequency;
	}

	public void setPollingFrequency(long pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

	public String getPathAttachments() {
		return pathAttachments;
	}

	public void setPathAttachments(String pathAttachments) {
		this.pathAttachments = pathAttachments;
	}
}
