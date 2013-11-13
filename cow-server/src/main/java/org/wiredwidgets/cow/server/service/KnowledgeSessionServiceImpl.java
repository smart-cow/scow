package org.wiredwidgets.cow.server.service;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.listener.JbpmProcessEventListener;
import org.wiredwidgets.cow.server.manager.RestServiceTaskHandler;

@Component
public class KnowledgeSessionServiceImpl implements KnowledgeSessionService {
	
    @Autowired
    KnowledgeBase kBase;
    
    @Autowired
    EntityManagerFactory emf;
    
    @Autowired
    PlatformTransactionManager txManager;
    
    @Autowired
    JbpmProcessEventListener processListener;
    
    private static Logger log = Logger.getLogger(KnowledgeSessionServiceImpl.class);
	
    @Override
	@Transactional
    public StatefulKnowledgeSession createInstance(){
    	
    	//EntityManager em = emf.createEntityManager();
    	//assert(em.isOpen());
    	
    	StatefulKnowledgeSession kSession;
        
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
            log.info("Could not find session with ID 1. Creating a new session");
            kSession = JPAKnowledgeService.newStatefulKnowledgeSession(kBase, config, env);
        }
        
        JPAProcessInstanceDbLog.setEnvironment(env);
        
        // em.close();
        kSession.addEventListener(processListener);
        
        return kSession;
    }
    
    @Transactional
	@Override
	public GenericHTWorkItemHandler createWorkItemHandler(
			StatefulKnowledgeSession session, RestServiceTaskHandler handle,
			org.jbpm.task.TaskService taskClient) {
    	
    	// EntityManager em = emf.createEntityManager();
    	// assert(em.isOpen());
    	
		GenericHTWorkItemHandler handler = new LocalHTWorkItemHandler(taskClient, session);
		handler.setLocal(true);
		
		session.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
		session.getWorkItemManager().registerWorkItemHandler("RestService", handle);

		return handler;
	}    

}
