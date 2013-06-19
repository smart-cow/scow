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
    
    public static Logger log = Logger.getLogger(TaskServiceFactory.class);
    
    
    private ThreadLocal<org.jbpm.task.TaskService> localTaskService = new ThreadLocal<org.jbpm.task.TaskService>() {
        /*
        * initialValue() is called
        */
        @Override
        protected org.jbpm.task.TaskService initialValue() {
	System.out.println("Creating TaskService for Thread : " + Thread.currentThread().getName());
        return new LocalTaskService(taskService);
        }
    };

    public org.jbpm.task.TaskService getTaskService() {
        return localTaskService.get();
    }    
    
}
