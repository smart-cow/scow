/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
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

package org.wiredwidgets.cow.server.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20NewProcessBuilder;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;

/**
 *
 * @author JKRANES
 */
@Transactional
@Component
public class ProcessDefinitionsServiceImpl extends AbstractCowServiceImpl implements ProcessDefinitionsService {
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	KnowledgeBase kbase;
	
    @Autowired
    Bpmn20NewProcessBuilder bpmn20ProcessBuilder;	
   
    @Autowired
    ProcessService processService;
	
    @Value("${rem2.url}")
    String REM2_URL;	

    private static TypeDescriptor COW_PROCESS_DEFINITION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessDefinition.class));
    
    private static Logger log = Logger.getLogger(ProcessDefinitionsServiceImpl.class);
    
	@Override
    public ProcessDefinition saveProcessDefinition(Process v2Process) {
       Definitions d = bpmn20ProcessBuilder.build(v2Process);
       processService.saveInRem2(v2Process);
       processService.loadWorkflow(d);
       return findLatestVersionProcessDefinitionByKey(v2Process.getKey());
    }    
    
    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findAllProcessDefinitions() {
    	return getDefsFromRem2();
    }
    

    @Transactional(readOnly = true)
    @Override
    @Deprecated
    public List<ProcessDefinition> findProcessDefinitionsByKey(String key) {
        // return this.convertProcessDefinitions(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list());
    	return new ArrayList<ProcessDefinition>();//throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean deleteProcessDefinitionsByKey(String key) {
    	return deleteProcessDefFromRem2(key);
    }    

    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition findLatestVersionProcessDefinitionByKey(String key) {
    	return getProcessDefinition(key);
    }

    /** Finds the latest version of each process definition
     *
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findLatestVersionProcessDefinitions() {
        // return this.convertProcessDefinitions(this.filterLatestVersions(repositoryService.createProcessDefinitionQuery().list()));	
    	return getDefsFromRem2();
    }

    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition getProcessDefinition(String id) {
    	return converter.convert(kbase.getProcess(id), ProcessDefinition.class);
    }
    
    @Override
	public String getRem2WorkflowLocation(){
        return this.REM2_URL +"/cms/workflows";        
    }    
    
    private List<ProcessDefinition> getDefsFromRem2() {
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
    
    private boolean deleteProcessDefFromRem2(String key) {
    	String url = REM2_URL + "/cms/workflows/" + key;
    	
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
        
}
