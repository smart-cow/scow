/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.marshalling.impl.ProtobufMessages.KnowledgeSession;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;

/**
 *
 * @author FITZPATRICK
 */
public class KnowledgeSessionFactory {
    @Autowired
    KnowledgeBase kBase;
    
    @Autowired
    EntityManagerFactory emf;
    
    @Autowired
    JpaTransactionManager txManager;
    
//    @Autowired
//    WorkItemHandler workItemHandler;
    
    private StatefulKnowledgeSession kSession;
    
    public static Logger log = Logger.getLogger(KnowledgeSessionFactory.class);
    
    public StatefulKnowledgeSession createInstance(){
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, txManager);
        
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);
        
        try{
            kSession = JPAKnowledgeService.loadStatefulKnowledgeSession(1, kBase, config, env);
        } catch (Exception e){
            log.error("Could not find session with ID 1. Creating a new session");
            kSession = JPAKnowledgeService.newStatefulKnowledgeSession(kBase, config, env);
        }
        
        JPAProcessInstanceDbLog.setEnvironment(env);
        
        return kSession;
        /*
        
        System.out.println("KBASE IS " + kBase);
        System.out.println("EMF IS " + emf);
        
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        kSession = JPAKnowledgeService.loadStatefulKnowledgeSession(4, kBase, null, env);*/
    }
    
}
