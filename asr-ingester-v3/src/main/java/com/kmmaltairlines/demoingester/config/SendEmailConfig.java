package com.kmmaltairlines.demoingester.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kmmaltairlines.mail.EmailRequest;

@Configuration
public class SendEmailConfig {
	
	@Value("${mail.recipients}")
	private String recipients;
	
	@Bean
	public EmailRequest emailRequest() {
		EmailRequest emailRequest = new EmailRequest();
		emailRequest.setSubject("Subject");
		emailRequest.setMessage("test");
		
		Set<String> recipientSet = Arrays.stream(recipients.split(","))
				.map(String::trim)
				.collect(Collectors.toSet());
		
		emailRequest.setRecipients(recipientSet);
		
		return emailRequest;
	}
	
}
