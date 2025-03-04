//package com.kmmaltairlines.demoingester;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.scheduling.annotation.EnableScheduling;
//
//import com.kmmaltairlines.demoingester.process.ProcessAsr;
//
//@SpringBootApplication(scanBasePackages = "com")
//@PropertySource("classpath:application.properties")
//@EnableScheduling
//public class DemoIngesterApplication {
//	
//	@Autowired
//	private ProcessAsr processASR;
//
//	public static void main(String[] args) {
//		SpringApplication.run(DemoIngesterApplication.class, args);
//	}
//
//	public void run(String...args) throws Exception{
//		processASR.processASRFile();
//	}
//}
