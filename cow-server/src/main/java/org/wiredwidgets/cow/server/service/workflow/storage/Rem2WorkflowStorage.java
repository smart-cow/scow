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
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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
    
    
	public URI save(org.wiredwidgets.cow.server.api.model.v2.Process process) {
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
        
        log.info("\nrem2.url = " + this.getRem2WorkflowLocation());
        URI location = restTemplate.postForLocation(getRem2WorkflowLocation(), node);
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
	public org.wiredwidgets.cow.server.api.model.v2.Process get(String key) {
    	String url = getRem2WorkflowLocation() + '/' + key;
    	try {
    		return restTemplate.getForObject(url, 
    				org.wiredwidgets.cow.server.api.model.v2.Process.class);
    	}
    	catch (HttpClientErrorException e) {
    		e.printStackTrace();
    		return null;
    	}
	}
	
	
	
	@Override
	public boolean delete(String key) {
    	String url = getRem2WorkflowLocation() + '/' + key;
    	try {
    		ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    	}
    	catch (HttpClientErrorException e) {
    		// REM2 returns 404 if the object is not found, which
    		// triggers this exception in RestTemplate
    		return false;
    	}
    	return true; 
	}
	
	
    private void addProperty(Node node, String name, String value) {
        Property p = new Property();
        p.setName(name);
        p.setValue(value);
        node.getProperties().add(p);
    }   
	
	
    
    private String getRem2WorkflowLocation(){
        return this.REM2_URL +"/cms/workflows";        
    }




}
