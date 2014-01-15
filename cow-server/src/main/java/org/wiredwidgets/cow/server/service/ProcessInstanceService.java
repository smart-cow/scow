/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.wiredwidgets.cow.server.api.service.ProcessInstance;

/**
 *
 * @author FITZPATRICK
 */
public interface ProcessInstanceService {
    /**
     * Find the specified process instance
     * @param id the process instance ID
     * @return
     */
    ProcessInstance getProcessInstance(Long id);

    /**
     * Execute the BPM process identified by the processDefinitionKey of the ProcessInstance 
     * passing in any variables included with the ProcessInstance
     * @param instance
     * @return the execution ID of the resulting ProcessInstance
     */
    String executeProcess(ProcessInstance instance);

    /**
     * Returns all running process instances in the BPM engine
     * @return
     */
    List<ProcessInstance> findAllProcessInstances();

    /**
     * Deletes a running process instance by its ID, including any sub-executions
     * @param id
     */
    boolean deleteProcessInstance(Long id);

    /**
     * Delete all process instances for a key
     * @param key
     */
    void deleteProcessInstancesByKey(String key);

    /**
     * Returns the process instance identified by the key
     * @param key
     * @return
     */
    List<ProcessInstance> findProcessInstancesByKey(String key);

    /**
     * Updates variables in the running process instance identified by the id of the ProcessInstance.
     * @param instance the argument must have non-null ID, which is used to identify the running process,
     * and should include any Variables to be added or updated.
     */
    boolean updateProcessInstance(ProcessInstance instance);
    
    /**
     * Returns a Process instance with its completion status attributes set according to the status
     * of a running process 
     * @param processInstanceId the process instance ID of the running process.
     * @return a Process object corresponding to the process Instance, with its completion attributes 
     * set according to the current state of the process instance.
     * 	
     */
    ProcessInstance getProcessInstanceStatus(Long processInstanceId);
    
    List<ProcessInstance> findAllHistoryProcessInstances();

    List<ProcessInstance> findHistoryProcessInstances(String key, Date endedAfter, boolean ended);

	public void signalProcessInstance(long id, String signal, String value);

	public abstract Map<String, Object> getProcessInstanceStatusGraph(Long processInstanceId);


}
