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

import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.api.service.ProcessDefinitions;
import org.wiredwidgets.cow.server.service.ProcessDefinitionsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles REST API for the /processDefinitions resource
 * @author JKRANES
 */
@Controller
@RequestMapping("/processDefinitions")
public class ProcessDefinitionsController extends CowServerController {

    @Autowired
    ProcessDefinitionsService processDefsService;

    private static Logger log = Logger.getLogger(ProcessDefinitionsController.class);

    /**
     * GET method for URL /processDefinitions.  Returns the latest version for each unique processDefinition key
     * Gets the latest version of each existing ProcessDefinition
     * @return a ProcessDefinitions object, wrapping multiple ProcessDefinition objects.
     */
    @RequestMapping(method=RequestMethod.GET, params={"!versions","!key"})
    @ResponseBody
    public ProcessDefinitions findLatestVersionProcessDefs() {
        ProcessDefinitions pd = new ProcessDefinitions();
        pd.getProcessDefinitions().addAll(processDefsService.findLatestVersionProcessDefinitions());
        return pd;
    }
    
    /**
     * GET method for URL /processDefinitions?versions=all
     * Gets all ProcessDefinitions, including all past versions of each
     * @return a ProcessDefinitions object, containing the requested results
     */
    @RequestMapping(method=RequestMethod.GET, params={"versions=all", "!key"})
    @ResponseBody
    public ProcessDefinitions findAllProcessDefs() {
        ProcessDefinitions pd = new ProcessDefinitions();
        pd.getProcessDefinitions().addAll(processDefsService.findAllProcessDefinitions());
        return pd;
    }

    /**
     * GET method for URL /processDefinitions?key={key}
     * Gets the latest version of a process definition, specified by key
     * @param key the key of the requested ProcessDefininition
     * @return a ProcessDefintions object containing a single ProcessDefinition element,
     * or containing no ProcessDefinition elements if none were found for the specified key
     */
    @RequestMapping(method=RequestMethod.GET, params={"key", "!versions"})
    @ResponseBody
    public ProcessDefinitions findLatestVersionProcessDefForKey(@RequestParam("key") String key) {
        ProcessDefinitions pd = new ProcessDefinitions();
        ProcessDefinition def = this.processDefsService.findLatestVersionProcessDefinitionByKey(key);
        if (def != null) {
            pd.getProcessDefinitions().add(def);
        }
        return pd;
    }
    
    /**
     * DELETE method for URL /processDefinitions?key={key}
     * Deletes all deployments for the specified key.  No executions of this process can be running.
     * @param key the process definition key
     */
    @RequestMapping(method=RequestMethod.DELETE, params={"key", "!versions"})
    public void deleteProcessDef(@RequestParam("key") String key, HttpServletResponse response) {
        if (processDefsService.deleteProcessDefinitionsByKey(key)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
        }
    }        

    /**
     * GET method for URL /processDefinitions?key={key}&versions=all
     * Retrieve all versions for the specified key
     * @param key the process definition key
     * @return a ProcessDefinitions object wrapping the results.
     */
    @RequestMapping(method=RequestMethod.GET, params={"key", "versions=all"})
    @ResponseBody
    public ProcessDefinitions findProcessDefsForKey(@RequestParam("key") String key) {
        ProcessDefinitions pd = new ProcessDefinitions();
        pd.getProcessDefinitions().addAll(processDefsService.findProcessDefinitionsByKey(key));
        return pd;
    }

    /**
     * GET method for URL /processDefinitions/{id}
     * Retrieve a single ProcessDefinition by ID
     * @param id the ProcessDefinition ID in the form [key]-[version]
     * @return
     */
    @RequestMapping(method=RequestMethod.GET, value="/{id}")
    @ResponseBody
    public ProcessDefinition getProcessDef(@PathVariable("id") String id) {
        return processDefsService.getProcessDefinition(decode(id));
    }
      
}
