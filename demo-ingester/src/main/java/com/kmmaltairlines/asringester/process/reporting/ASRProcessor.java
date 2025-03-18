package com.kmmaltairlines.asringester.process.reporting;

import java.util.List;

import com.kmmaltairlines.asringester.model.StationTransaction;

public interface ASRProcessor {
	ASRProcessReport processASRFile(String fileName, List<StationTransaction> transactions);
}
