package com.kmmaltairlines.demoingester.file;

import java.io.File;
import java.io.FilenameFilter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.mule.api.MuleMessage;
//import org.mule.api.routing.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ASRFileNameFilter implements FilenameFilter{ // Filter {

    public static final DateTimeFormatter ASR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    public static final Pattern ASR_FILE_NAME_PATTERN = Pattern.compile("(CERT|SABRE)_?KM_ASR_?(.*)_(\\d{8}_\\d{4}).cmp", Pattern.CASE_INSENSITIVE);
    private int delayInDays;
    
    private static final Logger LOG = LoggerFactory.getLogger(ASRFileNameFilter.class);
    
    @Override
    public boolean accept(File dir, String name) {
        return shouldProcessFile(name);
    }

    public boolean accept(String message) {
        // this serves absolutely no purposes other than to filter files on the FILE endpoint based on their name, 
        // not to filter files based on the MuleMessage content.
        return true;
    }
    
    /**
     * This method determines whether a file should be processed or not, based on the number of days configured in {@code delayInDays}.
     * 
     * If today is 2018-04-20, and delayInDays is configured to be '3', then we will process any file that arrived on 2018-04-17 or earlier.
     * 
     * @param fileName The name of the ASR file which contains transactions for a specific day
     * @return True to process the ASR file, false otherwise. 
     */
    private boolean shouldProcessFile(String fileName) {
        Matcher matcher = ASR_FILE_NAME_PATTERN.matcher(fileName.toUpperCase());
        boolean matchesRegex = matcher.find();
        
        if (!matchesRegex) {
            LOG.debug("{} does not match the specified regex {}. Skipping.", fileName.toUpperCase(), ASR_FILE_NAME_PATTERN.pattern());
            return false;
        }
        
        LocalDateTime fileDate = LocalDateTime.parse(matcher.group(3), ASR_DATE_FORMATTER);
        LocalDateTime today = LocalDateTime.now();
        
        // The idea here is that we process any files that are ON the day, or after x days.
        boolean shouldProcessFile = today.minusDays(delayInDays).isBefore(fileDate) == false;
        
        if (shouldProcessFile) {
            LOG.info("Processing file {}", fileName);
        }
        else {
            LOG.debug("Skipping file {} since it {} days have not yet passed.", fileName, delayInDays);
        }
        
        return shouldProcessFile;
    }
    
    public void setDelayInDays(int delayInDays) {
        this.delayInDays = delayInDays;
    }

}
