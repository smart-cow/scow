/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wiredwidgets.cow.server.service.workflow.storage;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.rem2.schema.Node;
import org.wiredwidgets.rem2.schema.Property;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;

public class Rem2WorkflowStorage implements IWorkflowStorage {
	
	private static Logger log = Logger.getLogger(Rem2WorkflowStorage.class);
	
    @Value("${rem2.url}")
    String REM2_URL;
    
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    protected Jaxb2Marshaller marshaller;
    
    
	public URI save(Process process) {
        Node node = new Node();
        node.setType("rem:marketplace");
        node.setName(process.getKey());
        
        addProperty(node, "rem:name", process.getKey());
        addProperty(node, "rem:description", process.getName());
        addProperty(node, "rem:type", "workflow");
        
        Node content = new Node();
        content.setName("jcr:content");
        content.setType("nt:resource");
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(process, new StreamResult(sw));
        
        addProperty(content, "jcr:data", sw.toString()); 
        addProperty(content, "jcr:mimeType", "application/xml");
        node.getNodes().add(content);
             
        log.debug("calling rem2");
        //RestTemplate restTemplate = new RestTemplate();
        //URI location = restTemplate.postForLocation("http://scout.mitre.org:8080/rem2/cms/workflows", node);
        
        log.info("\nrem2.url = " + getWorkflowsUrl());
        URI location = restTemplate.postForLocation(getWorkflowsUrl(), node);
        return location;
    } 
	
	
	public List<ProcessDefinition> getAll() {
    	List<ProcessDefinition> defs = new ArrayList<ProcessDefinition>();
    	
    	String url = REM2_URL + "/search/rem.rss?where=parent.[rem:type]%3D'workflow'";
    	URI uri;
    	try {
    		uri = new URI(url);    		
    	}
    	catch (URISyntaxException e) {
    		throw new RuntimeException("Invalid URI: " + url);
    	}
    	Channel channel = restTemplate.getForObject(uri, Channel.class);
    	// getItems() returns an untyped List
    	for(Object itemObj : channel.getItems()){
    		Item item = (Item)itemObj;
    		ProcessDefinition pd = new ProcessDefinition();
    		pd.setName(item.getTitle());
    		pd.setKey(item.getTitle());
    		defs.add(pd);
    	}
    	return defs;    	
	}
	
	

	
	@Override
	public Process get(String key) {
    	try {
    		return restTemplate.getForObject(getWorkflowUrl(key), Process.class);
    	}
    	catch (HttpClientErrorException e) {
    		log.error("Process: " + key + " was requested from REM server", e);
    		return null;
    	}
	}
	
	
	
	@Override
	public boolean delete(String key) {
    	try {
    		restTemplate.delete(getWorkflowUrl(key));
    		return true;
    	}
    	catch (HttpClientErrorException e) {
    		log.error("Process: " + key + " was requested from REM server", e);
    		return false;
    	}
	}
	
	
    private void addProperty(Node node, String name, String value) {
        Property p = new Property();
        p.setName(name);
        p.setValue(value);
        node.getProperties().add(p);
    }   
	
	
    
    private String getWorkflowsUrl() {
        return REM2_URL + "/cms/workflows";        
    }
    
    private String getWorkflowUrl(String key) {
    	return getWorkflowsUrl() + "/" + key;
    }




}
