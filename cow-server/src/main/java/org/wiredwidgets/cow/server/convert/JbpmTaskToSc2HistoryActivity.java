/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.convert;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.task.TaskData;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;

/**
 *
 * @author FITZPATRICK
 */
@Component
public class JbpmTaskToSc2HistoryActivity extends AbstractConverter<org.jbpm.task.Task, HistoryActivity> {

    // private static Logger log = Logger.getLogger(JbpmTaskToSc2HistoryTask.class);
    @Override
    public HistoryActivity convert(org.jbpm.task.Task s) {
        HistoryActivity target = new HistoryActivity();
        
        if (s == null) {
            return null;
        }
        
        // target.setId(String.valueOf(s.getId()));        
        
        TaskData td = s.getTaskData();
        if (td != null) {
            target.setStartTime(convert(td.getCreatedOn()));
            target.setEndTime(convert(td.getCompletedOn()));
            target.setExecutionId(td.getProcessId() + "." + String.valueOf(td.getProcessInstanceId()));
            // target.setState(td.getStatus().name());          
            if (s.getNames() != null && !s.getNames().isEmpty()){
            	// see Bpmn20UserTaskNodeBuilder
            	target.setActivityName(s.getNames().get(0).getText().split("/")[0]);
            }
            // target.setOutcome("jbpm_no_task_outcome_specified_jbpm");
            // target.setDuration(0);
        }        
        return target;
    }
    
}
