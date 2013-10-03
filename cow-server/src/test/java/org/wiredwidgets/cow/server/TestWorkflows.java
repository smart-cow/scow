package org.wiredwidgets.cow.server;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkflowProcessInstance;

// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration("/jbpm-config.xml")
public class TestWorkflows {
	
	//@Autowired
	//StatefulKnowledgeSession kSession;
	
	//@Autowired
	//KnowledgeBase kBase;

	@Test
	public void test() throws IOException {
		testWorkflow("script.bpmn", "script");    	
	}
		
	private void testWorkflow(String fileName, String processName) throws IOException {
        KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        KnowledgeBase kBase = kBuilder.newKnowledgeBase();
        
        
        InputStream stream = new ClassPathResource(fileName).getInputStream();
        Resource resource = ResourceFactory.newInputStreamResource(stream);
        kBuilder.add(resource, ResourceType.BPMN2);
        kBase.addKnowledgePackages(kBuilder.getKnowledgePackages()); 
        
        StatefulKnowledgeSession kSession = kBase.newStatefulKnowledgeSession();
        
        WorkflowProcessInstance instance = (WorkflowProcessInstance) kSession.startProcess(processName);
        // instance.setVariable("completed", "abcd");
        assertEquals(1, instance.getState());
        assertEquals("abcd", instance.getVariable("completed"));
	}

}
