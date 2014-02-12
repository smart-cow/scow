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

package org.wiredwidgets.cow.server.web;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

import java.util.Map;

import org.apache.log4j.Logger;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessInstances;
import org.wiredwidgets.cow.server.service.ProcessInstanceService;
import org.wiredwidgets.cow.server.service.ProcessService;

/**
 * Controller for REST operations for the /processes resource
 * @author JKRANES
 */
@Controller
@RequestMapping("/processes")
public class ProcessesController extends CowServerController {

    @Autowired
    ProcessService service;
    
    @Autowired
    ProcessInstanceService processInstanceService;
    
    private static Logger log = Logger.getLogger(ProcessesController.class);

    /**
     * Retrieve the process (workflow) XML in native (JPDL) format
     * @param key the process key
     * @return 
     */
    @RequestMapping(value = "/{key}", params = "format=native")
    @ResponseBody
    public Definitions getNativeProcess(@PathVariable("key") String key) {
        // return new StreamSource(processService.getNativeProcessAsStream(key));
    	return getBpmn20Process(key);
    }

    /**
     * Retrieves a workflow process in BPMN 2.0 format. This method only works for workflow processes
     * that were originally created in COW format.  
     * @param key the process key
     * @return the process in BPMN2.0 format
     */
    @RequestMapping(value = "/{key}", params = "format=bpmn20")
    @ResponseBody
    public Definitions getBpmn20Process(@PathVariable("key") String key) {
    	return service.getBpmn20Process(key);
    }    
    
    /**
     * Retrieves a workflow process in COW format.  This method only works for workflow
     * processes that were originally created in COW format.
     * @param key the process key.  Note: any "/" characters must be doubly encoded to "%252F"
     * @return the XML process document
     */
    @RequestMapping(value = "/{key}", params = "format=cow", produces="application/xml")
    @ResponseBody
    public org.wiredwidgets.cow.server.api.model.v2.Process getCowProcess(@PathVariable("key") String key) {
        return getV2Process(key);
    }  
    
    
    @RequestMapping(value = "/{id}")
    @ResponseBody
    public Process getProcess(@PathVariable("id") String id) {
    	return getCowProcess(id);
    }
    

    /**
     * For backward compatibility.  'cow' is preferred over 'v2'.
     * Calls getCowProcess
     * @param key
     * @return 
     * @see #getCowProcess(java.lang.String) 
     */
    @RequestMapping(value = "/{key}", params = "format=v2", produces="application/xml")
    @ResponseBody
    public org.wiredwidgets.cow.server.api.model.v2.Process getV2Process(@PathVariable("key") String key) {  
        return service.getV2Process(key);
    }
    
    
    
    @RequestMapping(value = "/{key}", params = "format=graph", produces="application/json")
    @ResponseBody
    public Map<String, Object> getCowProcessGraph(@PathVariable("key") String key) {
        return service.getProcessGraph(key);
    }
    
    
    /**
     * Retrieves the list of running instances for a given process
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/processInstances")
    @ResponseBody
    public ProcessInstances getProcessInstances(@PathVariable("id") String id) {
        ProcessInstances pi = new ProcessInstances();
        // note: decoding is applied to the id primarily to handle possible "/" characters
        pi.getProcessInstances().addAll(
        		processInstanceService.findProcessInstancesByKey(decode(id)));
        return pi;
    }
    
    
    /**
     * Create a new process. Attempts to use process.getKey() as the id. If the id is taken the
     * process's key will be set to a unique value. The response body will contain the process
     * with its key updated. The uri that can be used to get the newly created process can be 
     * found in the response's "Location" header.
     * 
     * @param process
     * @param uriBuilder
     * @return
     */
    @RequestMapping(method = POST)
    @ResponseBody
    public ResponseEntity<Process> createProcess(
    		@RequestBody Process process, UriComponentsBuilder uriBuilder) {
    	
    	String id = process.getKey();
    	id = getUniqueKey(id);
    	process.setKey(id);
    	service.save(process);
	
    	return getCreatedResponse("/processes/{id}", id, uriBuilder, getV2Process(id));
    }
    
    
    /**
     * Attempts to update the specified process. If the process updates normally the response
     * will have status code 200. If the process doesn't already exist it will be created
     * and the response will have status code 201.
     * A process cannot be modified when there are instances of it running. If there are instances
     * of the process running the response will have status code 409, and the body will contain
     * the running instances of the process.
     * 
     * @param id
     * @param process
     * @param uriBuilder
     * @return
     */
    @RequestMapping(value = "/{id}", method = PUT)
    @ResponseBody
    public ResponseEntity<?> updateProcess(
    		@PathVariable("id") String id, 
    		@RequestBody Process process, 
    		UriComponentsBuilder uriBuilder) {
    	
    	process.setKey(id);
    	Process existingProcess = service.getV2Process(id);
    	
    	if (existingProcess == null) {
    		//201 created
    		service.save(process);
    		return getCreatedResponse("/processes/{id}", id, uriBuilder, process);
    	}
    	
    	
    	ProcessInstances runningInstances = getProcessInstances(id);
    	
    	if (runningInstances.getProcessInstances().isEmpty()) {
    		//200 OK
    		service.save(process);
    		return new ResponseEntity<Process>(getV2Process(id), HttpStatus.OK);
    	}
    	else {
    		//409 need to delete process instances
    		return new ResponseEntity<ProcessInstances>(runningInstances, HttpStatus.CONFLICT);
    	}    	
    }
    
    
    /**
     * A process cannot be modified when there are instances of it running. If there are instances
     * of the process running the response will have status code 409, and the body will contain
     * the running instances of the process.
     * @param id
     * @return 204 if successful, 404 if not found, 409 if running instances
     */
    @RequestMapping(value = "/{id}", method = DELETE)
    @ResponseBody
    public ResponseEntity<?> deleteProcess(@PathVariable("id") String id) {
    	Process process = service.getV2Process(id);
    	if (process == null) {
    		return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
    	}
    	
    	ProcessInstances runningInstances = getProcessInstances(id);
    	if (runningInstances.getProcessInstances().isEmpty()) {
    		service.deleteProcess(id);
    		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    	}
    	else {
    		//409 need to delete process instances
    		return new ResponseEntity<ProcessInstances>(runningInstances, HttpStatus.CONFLICT);
    	}
    }
    
    private String getUniqueKey(String key)  {
    	String orginalKey = key;
    	int i = 1;
    	while (getCowProcess(key) != null) {
    		key = orginalKey + i;
    		i++;
    	}
    	return key;
    }
}
