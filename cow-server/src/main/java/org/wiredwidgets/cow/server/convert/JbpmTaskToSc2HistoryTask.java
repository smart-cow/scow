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
package org.wiredwidgets.cow.server.convert;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jbpm.task.TaskData;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.service.HistoryTask;

/**
 *
 * @author FITZPATRICK
 */
@Component
public class JbpmTaskToSc2HistoryTask extends AbstractConverter<org.jbpm.task.Task, HistoryTask> {

    // private static Logger log = Logger.getLogger(JbpmTaskToSc2HistoryTask.class);
    @Override
    public HistoryTask convert(org.jbpm.task.Task s) {
        HistoryTask target = new HistoryTask();
        
        if (s == null) {
            return null;
        }
        
        target.setId(String.valueOf(s.getId()));        
        
        TaskData td = s.getTaskData();
        if (td != null) {
            if (td.getActualOwner() != null) {
                target.setAssignee(td.getActualOwner().getId());
            }
            target.setCreateTime(convert(td.getCreatedOn()));
            target.setEndTime(convert(td.getCompletedOn()));
            target.setExecutionId(td.getProcessId() + "." + String.valueOf(td.getProcessInstanceId()));
            target.setState(td.getStatus().name());
            target.setOutcome("jbpm_no_task_outcome_specified_jbpm");
            target.setDuration(0);
        }        
        return target;
    }
    
}
