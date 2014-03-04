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
package org.wiredwidgets.cow.server.listener;

import java.util.List;

import javax.annotation.Resource;

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
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
/**
 *
 * @author FITZPATRICK
 */
@Component
public class JbpmProcessEventListener extends DefaultProcessEventListener{
    
    private static Logger log = Logger.getLogger(JbpmProcessEventListener.class);    
    
    @Autowired
    ConversionService converter;
    
    
    private List<ProcessInstancesListener> procInstancelisteners_;
    
    @Resource(name="processInstanceListeners")
    public void setListeners(List<ProcessInstancesListener> listeners) {
    	procInstancelisteners_ = listeners;
    }
  

    
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
    	//log.info("beforeProcessStarted: " + getInfo(event));
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
    	log.info("**afterProcessStarted: " + getInfo(event));
 
    	final org.wiredwidgets.cow.server.api.service.ProcessInstance pi = 
    			convert(event.getProcessInstance());
    	registerSync(new TransactionSynchronizationAdapter() {
    		public void afterCompletion(int i) {
    			for (ProcessInstancesListener pil : procInstancelisteners_) {
    				pil.onProcessStart(pi);
    			}
    		}
    	});
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
    	//log.info("beforeProcessCompleted: " + getInfo(event));
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
    	log.info("**afterProcessCompleted: " +  getInfo(event));
    	
    	final org.wiredwidgets.cow.server.api.service.ProcessInstance pi = 
    			convert(event.getProcessInstance());

    	registerSync(new TransactionSynchronizationAdapter() {
    		public void afterCompletion(int i) {
    			for (ProcessInstancesListener pil : procInstancelisteners_) {
    				pil.onProcessCompleted(pi);
    			}
    		}
    	});
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
    	//log.info("beforeNodeTriggered: " + getNodeInfo(event));
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
    	//log.info("afterNodeTriggered: " + getNodeInfo(event));
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
    	//log.info("beforeNodeLeft: " + getNodeInfo(event));
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
    	//NodeInstance node = event.getNodeInstance();
    	//log.info("After node left: " + getNodeInfo(event));
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
    
	private static void registerSync(TransactionSynchronizationAdapter syncAdapter) {
		TransactionSynchronizationManager.registerSynchronization(syncAdapter);
	}
	
	
	private org.wiredwidgets.cow.server.api.service.ProcessInstance 
		convert(org.drools.runtime.process.ProcessInstance pi) {
		return converter.convert(pi, 
				org.wiredwidgets.cow.server.api.service.ProcessInstance.class);
	}
	
    
    private String getNodeInfo(ProcessNodeEvent event) {
    	return event.getProcessInstance().getProcessId() + "." + event.getProcessInstance().getId()
    			+ " (" + event.getNodeInstance().getNodeName() + ")";
    }
    
}
