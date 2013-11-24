package org.wiredwidgets.cow.server.transform.graph;

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
import org.wiredwidgets.cow.server.transform.graph.bpmn20.Bpmn20NewProcessBuilder;
import org.wiredwidgets.cow.server.transform.graph.builder.GraphBuilder;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/jaxb-test-context.xml")
public class TestGeneration {
	
	@Autowired
	Jaxb2Marshaller marshaller;
	
	@Autowired
	Bpmn20NewProcessBuilder builder;
	
	@Autowired
	GraphBuilder graphBuilder;
		
	private static Logger log = Logger.getLogger(TestGeneration.class);
	
	@Test
	public void testNewGraph() {
		Process process = unmarshalFromClassPathResource("v2-simple.xml", Process.class);
		DirectedGraph<Activity, ActivityEdge> graph = graphBuilder.buildGraph(process);
		
		
		DepthFirstIterator<Activity, ActivityEdge> it = new DepthFirstIterator<Activity, ActivityEdge>(graph);
		
		while (it.hasNext()) {
			Activity activity = it.next();
			for (ActivityEdge edge : graph.outgoingEdgesOf(activity)) {
				log.info(activity.getName() + "->" + graph.getEdgeTarget(edge).getName());
			}
		}
		
		assertTrue(true);
		
	}
	
	@Test
	public void testSequential() {
		Process process = unmarshalFromClassPathResource("user-tasks-only.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}
	
	@Test
	public void testParallel() {
		Process process = unmarshalFromClassPathResource("parallel-list-test.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}	
	
	@Test
	public void testCsar() {
		Process process = unmarshalFromClassPathResource("csar_high_level.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}	
	
	@Test
	public void testDayInLife() {
		Process process = unmarshalFromClassPathResource("day_in_life.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}
	
	@Test
	public void testBypass() {
		Process process = unmarshalFromClassPathResource("bypass.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}
	
	@Test
	public void testDecision() {
		Process process = unmarshalFromClassPathResource("v2-decision.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}	
	
	@Test
	public void testExit() {
		Process process = unmarshalFromClassPathResource("exit-test.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
		assertTrue(true);
	}
	
	@Test
	public void testDecisionTest() {
		Process process = unmarshalFromClassPathResource("decisionTest.xml", Process.class);
		Definitions defs = builder.build(process);
		String xml = marshalToString(defs);
		log.info("xml: " + xml);
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
