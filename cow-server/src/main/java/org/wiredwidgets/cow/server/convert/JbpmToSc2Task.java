/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.convert;

import org.jbpm.task.TaskData;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 *
 * @author FITZPATRICK
 */
public class JbpmToSc2Task extends AbstractConverter<org.jbpm.task.Task, Task>{

    @Override
    public Task convert(org.jbpm.task.Task source) {
        
        Task target = new Task();
        TaskData td = source.getTaskData();

        if (source.getDescriptions() != null){
            target.setDescription(source.getDescriptions().get(0).getText());
        }

        if (td.getActualOwner() != null){
            target.setAssignee(td.getActualOwner().getId());
        }

        if (td.getCreatedOn() != null) {
            target.setCreateTime(convert(td.getCreatedOn()));
        }

        if (td.getExpirationTime() != null) {
            target.setDueDate(convert(td.getExpirationTime()));
        }
        
        target.setId(String.valueOf(source.getId()));
        target.setPriority(new Integer(source.getPriority()));
        target.setProcessInstanceId(td.getProcessId() + "." + String.valueOf(td.getProcessInstanceId()));
        
        if (source.getNames() != null && source.getNames().size() > 0) {
	        String[] parts = source.getNames().get(0).getText().split("/");
	        target.setActivityName(parts[0]);
	        target.setName(parts[1]);      
        }

        // add variables
        /*Set<String> names = taskService.getVariableNames(source.getId());
        Map<String, Object> variables = taskService.getVariables(source.getId(), names);
        // remove process name var
        variables.remove("_name");
        for (String key : variables.keySet()) {
            Variable var = new Variable();
            var.setName(key);
            // Support strings only.  Other types will cause ClassCastException
            try {
                var.setValue((String) variables.get(key));
            } catch (ClassCastException e) {
                var.setValue("Variable type " + variables.get(key).getClass().getName() + " is not supported");
            }
            addVariable(target, var);
        }

        // Do this only if the task is not an ad-hoc task (as indicated by null executionId)
        if (source.getExecutionId() != null) {

            // name is the 'form' attribute in JPDL
            // this is used in the COW schema to store the display name, as distinct from the system-generated name
            target.setName(source.getFormResourceName());
            
            // activityName is the 'name' from JPDL
            target.setActivityName(source.getActivityName());            

            Execution ex = executionService.findExecutionById(source.getExecutionId());
            target.setProcessInstanceId(ex.getProcessInstance().getId());

            // outcomes
            Set<String> outcomes = taskService.getOutcomes(source.getId());
            for (String outcome : outcomes) {
                target.getOutcomes().add(outcome);
            }

            // Workaround to the fact that we cannot use autowiring here
            if (this.cowTaskService == null) {
                this.cowTaskService = (org.wiredwidgets.cow.server.service.TaskService) this.factory.getBean("taskService");
            }

            // add process level task varibles (
            String executionId = getTopLevelExecutionId(source.getExecutionId());
            org.wiredwidgets.cow.server.api.model.v2.Activity activity = cowTaskService.getWorkflowActivity(executionId, source.getActivityName());
            if (activity != null && activity instanceof org.wiredwidgets.cow.server.api.model.v2.Task) {
                org.wiredwidgets.cow.server.api.model.v2.Task cowTask = (org.wiredwidgets.cow.server.api.model.v2.Task) activity;
                if (cowTask.getVariables() != null) {
                    for (org.wiredwidgets.cow.server.api.model.v2.Variable var : cowTask.getVariables().getVariables()) {
                        Variable newVar = new Variable();
                        newVar.setName(var.getName());
                        newVar.setValue(var.getValue());
                        addVariable(target, newVar);
                    }
                }
            }
        } else {
            // for ad-hoc tasks
            target.setName(source.getName());
        }*/

        return target;
    }
  
}
