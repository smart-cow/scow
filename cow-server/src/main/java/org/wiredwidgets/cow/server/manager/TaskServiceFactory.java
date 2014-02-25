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
package org.wiredwidgets.cow.server.manager;

import org.apache.log4j.Logger;
import org.jbpm.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.jbpm.task.service.local.LocalTaskService;

/**
 *
 * @author PREMA
 */
@Component
public class TaskServiceFactory {
    
    @Autowired
    org.jbpm.task.service.TaskService taskService;
    
    private static Logger log = Logger.getLogger(TaskServiceFactory.class);
    
    
    private ThreadLocal<org.jbpm.task.TaskService> localTaskService = new ThreadLocal<org.jbpm.task.TaskService>() {
        /*
        * initialValue() is called
        */
        @Override
        protected org.jbpm.task.TaskService initialValue() {
        	log.info("Creating TaskService for Thread : " + Thread.currentThread().getName());
        	return new LocalTaskService(taskService);
        }
    };

    public org.jbpm.task.TaskService getTaskService() {
        return localTaskService.get();
    }    
    
  
}
