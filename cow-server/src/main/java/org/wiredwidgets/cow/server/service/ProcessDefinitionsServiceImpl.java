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

package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;
import org.wiredwidgets.cow.server.service.workflow.storage.IWorkflowStorage;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20ProcessBuilder;

/**
 *
 * @author JKRANES
 */
@Transactional
@Component
public class ProcessDefinitionsServiceImpl extends AbstractCowServiceImpl implements ProcessDefinitionsService {
	
	@Autowired
	KnowledgeBase kbase;
	
    @Autowired
    Bpmn20ProcessBuilder bpmn20ProcessBuilder;	
   
    @Autowired
    ProcessService processService;
    
    @Resource
    IWorkflowStorage workflowStorage;
	

    private static TypeDescriptor COW_PROCESS_DEFINITION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessDefinition.class));
    
    private static Logger log = Logger.getLogger(ProcessDefinitionsServiceImpl.class);
    
	@Override
    public ProcessDefinition saveProcessDefinition(Process v2Process) {
       Definitions d = bpmn20ProcessBuilder.build(v2Process);
       processService.save(v2Process);
       processService.loadWorkflow(d);
       return findLatestVersionProcessDefinitionByKey(v2Process.getKey());
    }    
    
    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findAllProcessDefinitions() {
    	return workflowStorage.getAll();
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
    	return workflowStorage.delete(key);
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
    	//return getDefsFromRem2();
    	return workflowStorage.getAll();
    }

    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition getProcessDefinition(String id) {
    	return converter.convert(kbase.getProcess(id), ProcessDefinition.class);
    }
    
           
}
