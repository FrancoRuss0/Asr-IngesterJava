package com.kmmaltairlines.demoingester.process.reporting;

import java.util.List;

import com.kmmaltairlines.demoingester.model.StationTransaction;

public interface ASRProcessor {
	ASRProcessReport processASRFile(String fileName, List<StationTransaction> transactions);
}
