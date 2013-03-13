/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
/**
 *
 * @author FITZPATRICK
 */
public class JbpmProcessEventListener implements ProcessEventListener{
    
    private static Logger log = Logger.getLogger(JbpmProcessEventListener.class);
    
    @Autowired
    AmqpTemplate amqp;
    
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
    	// Task task = taskService.getTask(event.getNodeInstance().getNodeId());
    	
    	if (event.getNodeInstance() instanceof HumanTaskNodeInstance) {
    		HumanTaskNodeInstance ni = (HumanTaskNodeInstance)event.getNodeInstance();
   
				Long taskId = ni.getWorkItemId();
    	
				Long processInstanceId = ni.getProcessInstance().getId();
				String processName = ni.getProcessInstance().getProcessName();
				
				String info = "eventType=TaskReady;processID="
						+ processName + "." + processInstanceId + ";" + "taskID=" + taskId;
				
				log.info("sending message: " + info);
				amqp.convertAndSend("amqp.topic", "process", info);
    	}
    	
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void getSessionProcessListener() {
        
    }

    public void setSessionProcessListener(StatefulKnowledgeSession kSession) {
        kSession.addEventListener(this);
    }
    
}
