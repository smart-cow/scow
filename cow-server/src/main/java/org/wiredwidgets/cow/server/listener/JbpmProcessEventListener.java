/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.listener;

import org.apache.log4j.Logger;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEvent;
import org.drools.event.process.ProcessNodeEvent;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.runtime.process.NodeInstance;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 *
 * @author FITZPATRICK
 */
@Component
public class JbpmProcessEventListener extends DefaultProcessEventListener{
    
    private static Logger log = Logger.getLogger(JbpmProcessEventListener.class);
    
    @Autowired
    AmqpTemplate amqp;
    
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
    	log.info("beforeProcessStarted: " + getInfo(event));
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
    	log.info("afterProcessStarted: " + getInfo(event));
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
    	log.info("beforeProcessCompleted: " + getInfo(event));
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
    	log.info("afterProcessCompleted: " + getInfo(event));
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
    	log.info("beforeNodeTriggered: " + getNodeInfo(event));
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
    	log.info("afterNodeTriggered: " + getNodeInfo(event));
//    	if (event.getNodeInstance() instanceof HumanTaskNodeInstance) {
//    		HumanTaskNodeInstance ni = (HumanTaskNodeInstance)event.getNodeInstance();
//   
//				Long taskId = ni.getWorkItemId();
//    	
//				Long processInstanceId = ni.getProcessInstance().getId();
//				String processName = ni.getProcessInstance().getProcessName();
//				
//				String info = "eventType=TaskReady;processID="
//						+ processName + "." + processInstanceId + ";" + "taskID=" + taskId;
//				
//				log.info("sending message: " + info);
//				amqp.convertAndSend("amqp.topic", "process", info);
//    	}
    	
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
    	log.info("beforeNodeLeft: " + getNodeInfo(event));
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
    	NodeInstance node = event.getNodeInstance();
    	log.info("After node left: " + getNodeInfo(event));
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private String getInfo(ProcessEvent event) {
    	return event.getProcessInstance().getProcessId() + "." + event.getProcessInstance().getId();
    }
    
    private String getNodeInfo(ProcessNodeEvent event) {
    	return event.getProcessInstance().getProcessId() + "." + event.getProcessInstance().getId()
    			+ " (" + event.getNodeInstance().getNodeName() + ")";
    }
    
}
