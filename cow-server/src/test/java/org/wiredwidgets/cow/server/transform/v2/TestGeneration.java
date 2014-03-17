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

package org.wiredwidgets.cow.server.transform.v2;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityEdge;
import org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20ProcessBuilder;
import org.wiredwidgets.cow.server.transform.graph.builder.GraphBuilder;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/jaxb-test-context.xml")
public class TestGeneration {
	
	@Autowired
	Jaxb2Marshaller marshaller;
	
	@Autowired
	Bpmn20ProcessBuilder builder;
	
	@Autowired
	GraphBuilder graphBuilder;
		
	private static Logger log = Logger.getLogger(TestGeneration.class);
	
	@Test
	public void test() {
		
		Process process = unmarshalFromClassPathResource("v2-simple.xml", Process.class);
		Definitions def = builder.build(process);
		assertTrue(true);
	}
	
	private <T> T unmarshalFromClassPathResource(String resourceName, Class<T> type) {
		try {
			ClassPathResource cpr = new ClassPathResource(resourceName);
			return (T) marshaller.unmarshal(new StreamSource(cpr.getInputStream()));
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String marshalToString(Object object) {
		StringWriter sw = new StringWriter();
		marshaller.marshal(object, new StreamResult(sw));
		return sw.toString();
	}

}
