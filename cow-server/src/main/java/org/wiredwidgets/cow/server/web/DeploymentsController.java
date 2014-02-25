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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.web;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.Deployment;
import org.wiredwidgets.cow.server.api.service.Deployments;
import org.wiredwidgets.cow.server.service.ProcessService;

/**
 * Controller to handle REST calls for the /deployments resource
 * @author FITZPATRICK
 */
@Controller
@RequestMapping("/deployments")
public class DeploymentsController {
    
    @Autowired
    ProcessService processService;
    
    static Logger log = Logger.getLogger(DeploymentsController.class);
    
    /**
     * Create or update a workflow in native JPDL.
     * @param source A valid JPDL 4.4 workflow document
     * @param name The name of the deployment.  By convention this should be the same as the "name" attribute of the process element.
     * @param bpmn2 Future capability; not currently implemented.  Set to true if the workflow is in BPMN 2.0 format.
     * @return a Deployment object as XML
     */
    @RequestMapping(value = "/native", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public Deployment createDeployment(@RequestBody Definitions definitions, @RequestParam String name, @RequestParam(value = "bpmn2", required = false) boolean bpmn2) {
        return processService.createDeployment(definitions);
    }
    
    /**
     * Retrieve a Deployment object
     * @param id the ID of the deployment
     * @return the Deployment object as XML
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Deployment getDeployment(@PathVariable("id") String id) {
        return processService.getDeployment(id);
    }
    
    /**
     * Retrieve all deployments
     * Deprecated: use GET /processDefinitions instead to get a list of existing
     * process defininitions.
     * @return a Deployments object, which wraps multiple Deployment objects
     * @throws Exception 
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @Deprecated
    public Deployments listDeployments() throws Exception {
        Deployments deployments = new Deployments();
        deployments.getDeployments().addAll(processService.findAllDeployments());
        return deployments;
    }
    
    /**
     * Delete a Deployment.  A deployment cannot be deleted if it has running executions
     * @param id the ID of the deployment
     * @param response 204 if successful, 404 if not found
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteDeployment(@PathVariable("id") String id, HttpServletResponse response) {
        Deployment deployment = processService.getDeployment(id);
        if (deployment == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
        } else {
            processService.deleteDeployment(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
        }

    }
    
    /**
     * Create or update a workflow process using a 'cow' format file.
     * @param process a valid COW format workflow document
     * @return  a Deployment object as XML
     */
    @RequestMapping(value = "/cow", method = RequestMethod.POST)
    @ResponseBody
    public Deployment createCowDeployment(@RequestBody Process process) {
        return createV2Deployment(process);
    }
   
    /**
     * For backward compatibility.  'cow' is preferred.
     * @param process
     * @return 
     * @see #createCowDeployment(Process, String)
     */
    @RequestMapping(value = "/v2", method = RequestMethod.POST)
    @ResponseBody
    public Deployment createV2Deployment(@RequestBody Process process) {
        return processService.saveV2Process(process);
    }
    
}
