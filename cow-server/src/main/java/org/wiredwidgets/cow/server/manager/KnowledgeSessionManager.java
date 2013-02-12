/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.drools.runtime.StatefulKnowledgeSession;

/**
 *
 * @author FITZPATRICK
 */
public class KnowledgeSessionManager {
    StatefulKnowledgeSession kSession;
    
    public void init() {
        
    }
    
    public StatefulKnowledgeSession getkSession() {
        return kSession;
    }
    
    public void setkSession(StatefulKnowledgeSession kSession) {
        this.kSession = kSession;
    }
}
