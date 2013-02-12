/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.listener;

import org.drools.event.process.*;
import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
/**
 *
 * @author FITZPATRICK
 */
public class JbpmProcessEventListener implements ProcessEventListener{
    
    private static Logger log = Logger.getLogger(JbpmProcessEventListener.class);

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
        //throw new UnsupportedOperationException("Not supported yet.");
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
