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

import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public StreamSource getCowProcess(@PathVariable("key") String key) {
        return getV2Process(key);
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
    public StreamSource getV2Process(@PathVariable("key") String key) {  
        return new StreamSource(service.getResourceAsStream(decode(key), ProcessService.V2_EXTENSION));
    }
    
    @RequestMapping(value = "/{key}", params = "format=graph", produces="application/json")
    @ResponseBody
    public Map<String, Object> getCowProcessGraph(@PathVariable("key") String key) {
        return service.getProcessGraph(key);
    }      
}
