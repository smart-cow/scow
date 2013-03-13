/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiredwidgets.cow.server.service.KnowledgeSessionService;

/**
 *
 * @author FITZPATRICK
 */
public class KnowledgeSessionFactory {

	@Autowired
	KnowledgeSessionService service;
    
    public static Logger log = Logger.getLogger(KnowledgeSessionFactory.class);
    
    public StatefulKnowledgeSession createInstance(){
    	// need to wrap this in a transaction
    	return service.createInstance();
    }
    
}
