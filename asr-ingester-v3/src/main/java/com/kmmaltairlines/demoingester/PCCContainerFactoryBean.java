package com.kmmaltairlines.demoingester;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class PCCContainerFactoryBean implements FactoryBean<Set<String>>, InitializingBean {
	
	@Value("${clients.paxport.pccs}")
	private String paxportPCC;
	
	@Value("${clients.ryanair.pccs}")
	private String ryanairPCC;
	
	@Value("${clients.sabredx.pccs}")
	private String sabredxPCC;
	
	@Value("${clients.website.pccs}")
	private String websitePCC;

    private Set<String> paxportSet;
    private Set<String> ryanairSet;
    private Set<String> sabredxSet;
    private Set<String> websiteSet;
    
    public PCCContainerFactoryBean() {
        System.out.println("Creating PCCContainerFactoryBean instance: " + this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	if (StringUtils.isAnyBlank(paxportPCC, ryanairPCC, sabredxPCC, websitePCC)) {
            throw new IllegalArgumentException("Tutti i valori PCC devono essere configurati correttamente.");
        }
    	
    	paxportSet = parsePCCs(paxportPCC);
        ryanairSet = parsePCCs(ryanairPCC);
        sabredxSet = parsePCCs(sabredxPCC);
        websiteSet = parsePCCs(websitePCC);
    }
    
    private Set<String> parsePCCs(String pccs) {
        return Stream.of(pccs.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }
    
    public Set<String> getPaxportPCCs() {
        return paxportSet;
    }

    public Set<String> getRyanairPCCs() {
        return ryanairSet;
    }

    public Set<String> getSabreDXPCCs() {
        return sabredxSet;
    }

    public Set<String> getWebsitePCCs() {
        return websiteSet;
    }
    
    @Override
    public Set<String> getObject() {
        return Stream.of(paxportSet, ryanairSet, sabredxSet, websiteSet)
        		.flatMap(Set::stream)
        		.collect(Collectors.toSet());
    }
    
    @Override
    public Class<?> getObjectType() {
        return Set.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }   
    
    public boolean isPCC(String pcc, Set<String> pccSet) {
    	return pccSet.contains(pcc.trim().toUpperCase());
    }
    
    public boolean isPaxport(String pcc) {
    	return isPCC(pcc, paxportSet);
    }
    
    public boolean isRyanair(String pcc) {
    	return isPCC(pcc, ryanairSet);
    }
    
    public boolean isSabredx(String pcc) {
    	return isPCC(pcc, sabredxSet);
    }
    
    public boolean isWebsite(String pcc) {
    	return isPCC(pcc, websiteSet);
    }
}
