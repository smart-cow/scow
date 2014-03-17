package org.wiredwidgets.cow.server.jbpm;

import javax.persistence.EntityManagerFactory;

import org.drools.runtime.EnvironmentName;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class JbpmConfig {
	
	@Autowired
	@Qualifier("emf") // this EMF is the one used for JBPM (as opposed to the one for Returns)
	EntityManagerFactory emf;	
	
    @Autowired
    JtaTransactionManager txManager;
    
    @Autowired
    CowRegisterableItemsFactory cowRegisterableItemsFactory;
    
//    public @Bean KieBase kieBase() {
//    	KieHelper helper = new KieHelper();
//    	KieBase kieBase = helper.addResource(ResourceFactory.newClassPathResource("EDA.1040_HappyPath_2.bpmn")).build();
//    	return kieBase;
//    }
	
	public @Bean RuntimeManager runtimeManager() {
		
		
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
				.newDefaultBuilder()
				.entityManagerFactory(emf)
				.addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, txManager.getTransactionManager())
				.addEnvironmentEntry("IS_JTA_TRANSACTION", true)
				.registerableItemsFactory(cowRegisterableItemsFactory)
				.userGroupCallback(new CowUserGroupCallback())
				.persistence(true)
//				.addAsset(ResourceFactory.newClassPathResource("EDA.1040_HappyPath.bpmn2"), ResourceType.BPMN2)
//				.addAsset(ResourceFactory.newClassPathResource("EDA.1040_HappyPath_2.bpmn2"), ResourceType.BPMN2)
//				.addAsset(ResourceFactory.newClassPathResource("EDA.1040_HappyPath_nolane.bpmn2"), ResourceType.BPMN2)
//				.addAsset(ResourceFactory.newClassPathResource("ruleset.drl"), ResourceType.DRL)
				.get();
		
		
		RuntimeManager manager = RuntimeManagerFactory.Factory.get()
				.newPerRequestRuntimeManager(environment);
		
		return manager;
		
	}

}
