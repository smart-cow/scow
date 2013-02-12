/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.MinaHTWorkItemHandler;
import org.jbpm.task.Content;
import org.jbpm.task.Task;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.SyncTaskServiceWrapper;
import org.jbpm.task.service.mina.AsyncMinaTaskClient;
import org.jbpm.task.utils.ContentMarshallerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author FITZPATRICK
 */
public class TestHumanVars {
    @Autowired
    StatefulKnowledgeSession kSession;
    
    @Autowired
    protected org.jbpm.task.TaskService taskClient;
    
    @Autowired
    protected MinaHTWorkItemHandler minaWorkItemHandler;
    
    public void testHumanVars(){
        /*Map<String, Object> params = new HashMap<String, Object>();
        params.put("varOne", "Here is variable one");
        kSession.startProcess("TestVariable", params);
        
        org.jbpm.task.TaskService taskClient = new SyncTaskServiceWrapper(new AsyncMinaTaskClient());
        taskClient.connect("127.0.0.1", 9123);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        TaskSummary task1 = taskClient.getTasksAssignedAsPotentialOwner("shawn", "en-UK").get(0);
        System.out.println("Shawn starting task " + task1.getName() + " ID: " + task1.getId());
        taskClient.start(task1.getId(), "shawn");
        
        Map<String,Object> results = new HashMap<String,Object>();
        results.put("varTwo", "Here is variable two");
        ContentData contentData = ContentMarshallerHelper.marshal(results, minaWorkItemHandler.getMarshallerContext(), null);
        
        taskClient.complete(task1.getId(), "shawn", contentData);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        TaskSummary task2 = taskClient.getTasksAssignedAsPotentialOwner("shawn", "en-UK").get(0);
        System.out.println("Shawn starting task " + task2.getName() + " ID: " + task2.getId());
        taskClient.start(task2.getId(), "shawn");
        
        results = new HashMap<String, Object>();
        results.put("varThree", "Here is variable three");
        contentData = ContentMarshallerHelper.marshal(results, minaWorkItemHandler.getMarshallerContext(), null);
        
        taskClient.complete(task2.getId(), "shawn", contentData);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        TaskSummary task3 = taskClient.getTasksAssignedAsPotentialOwner("shawn", "en-UK").get(0);
        System.out.println("Shawn starting task " + task3.getName() + " ID: " + task3.getId());
        taskClient.start(task3.getId(), "shawn");
        
        Task task = taskClient.getTask(task3.getId());
        
        Content content = taskClient.getContent(task.getTaskData().getDocumentContentId());
        
        Object result = ContentMarshallerHelper.unmarshall("org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy", content.getContent(), minaWorkItemHandler.getMarshallerContext(), null);
        Map<?,?> map = (Map<?,?>)result;
        for (Map.Entry<?,?> entry : map.entrySet()){
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        taskClient.complete(task3.getId(), "shawn", null);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            taskClient.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    public void startWorkflow(){
        
    }
    
    public void completeTask(Long id, String assignee, Map <String,Object> results){
        //org.jbpm.task.TaskService taskClient = new SyncTaskServiceWrapper(new AsyncMinaTaskClient());
        //taskClient.connect("127.0.0.1", 9123);
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
        //TaskSummary task1 = taskClient.getTasksAssignedAsPotentialOwner("shawn", "en-UK").get(0);
        
        //System.out.println("Shawn starting task " + task1.getName() + " ID: " + task1.getId());
        System.out.println(assignee + " starting task with ID: " + id);
        
        //taskClient.start(task1.getId(), "shawn");
        taskClient.start(id, assignee);
        
        //Task task = taskClient.getTask(task1.getId());
        Task task = taskClient.getTask(id);
        
       /* Content content = taskClient.getContent(task.getTaskData().getDocumentContentId());
        
        Object result = ContentMarshallerHelper.unmarshall("org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy", content.getContent(), minaWorkItemHandler.getMarshallerContext(), null);
        Map<?,?> map = (Map<?,?>)result;
        for (Map.Entry<?,?> entry : map.entrySet()){
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        
        ContentData contentData = ContentMarshallerHelper.marshal(results, minaWorkItemHandler.getMarshallerContext(), null);*/
        
        //taskClient.complete(task1.getId(), "shawn", contentData);
        //taskClient.complete(id, assignee, contentData);
        
        
        /*try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        /*try {
            taskClient.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(TestHumanVars.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
}
