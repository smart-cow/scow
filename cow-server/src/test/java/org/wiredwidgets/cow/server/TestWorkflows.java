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
