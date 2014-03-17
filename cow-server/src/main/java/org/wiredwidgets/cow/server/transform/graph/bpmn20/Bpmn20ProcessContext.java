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

package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.omg.spec.bpmn._20100524.di.BPMNDiagram;
import org.omg.spec.bpmn._20100524.di.BPMNEdge;
import org.omg.spec.bpmn._20100524.di.BPMNPlane;
import org.omg.spec.bpmn._20100524.di.BPMNShape;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.omg.spec.bpmn._20100524.model.ExtensionElements;
import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TItemDefinition;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.TProcessType;
import org.omg.spec.bpmn._20100524.model.TSequenceFlow;
import org.omg.spec.dd._20100524.dc.Bounds;
import org.omg.spec.dd._20100524.dc.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.AbstractProcessContext;


/**
 * 
 * 
 * 
 * @author JKRANES
 */
public class Bpmn20ProcessContext extends AbstractProcessContext<Bpmn20Node, TProcess> {
	
	private static final String DROOLS_NAMESPACE = "http://www.jboss.org/drools";
	private static final String JAVA_TYPES_NAMESPACE = "http://www.java.com/javaTypes";
	private static final String MVEL_NAMESPACE = "http://www.mvel.org/2.0";
	private static final String BPMN20_MODEL_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
	private static final String DEFINITIONS_ID = "Definitions"; // unclear whether this has a any meaning

    private static Logger log = Logger.getLogger(Bpmn20ProcessContext.class);

    private Definitions definitions;
    private Document doc;
    private BPMNPlane plane;
    private int xPosition = 100;
    private static org.omg.spec.bpmn._20100524.di.ObjectFactory diFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
    private static org.omg.spec.bpmn._20100524.model.ObjectFactory modelFactory = new org.omg.spec.bpmn._20100524.model.ObjectFactory();
    private Map<String, Property> properties = new HashMap<String, Property>();
    private Set<String> imports = new HashSet<String>();
        
    public Bpmn20ProcessContext(Process source, TProcess target) {
        super(source, target);
        initialize(source, target);
    }

    @Override
    public void addNode(Bpmn20Node node) {
        getTarget().getFlowElements().add(node.getNode());
        BPMNShape shape = new BPMNShape();
        shape.setBpmnElement(new QName(node.getNode().getValue().getId()));
        Bounds b = new Bounds();
        b.setHeight(50);
        b.setWidth(50);
        b.setX(xPosition +=100);
        b.setY(100);
        shape.setBounds(b);
        plane.getDiagramElements().add(diFactory.createBPMNShape(shape));
    }
    
    public void addEdge(TSequenceFlow sequenceFlow) {
    	getTarget().getFlowElements().add(modelFactory.createSequenceFlow(sequenceFlow));
        BPMNEdge edge = new BPMNEdge();
        edge.setBpmnElement(new QName(sequenceFlow.getId()));
        Point waypoint1 = new Point();
        Point waypoint2 = new Point();
        // waypoints are required for schema validation
        // values are arbitrary until we have a real layout algorithm
        waypoint1.setX(50);
        waypoint1.setY(100);
        waypoint2.setX(50);
        waypoint2.setY(100);
        edge.getWaypoints().add(waypoint1);
        edge.getWaypoints().add(waypoint2);       
        plane.getDiagramElements().add(diFactory.createBPMNEdge(edge));    	
    }
     
    
    /*
     * Defines a process level variable with <property> and <itemDefinition> elements
     * <property itemSubjectRef="_{itemName}Item" id="{itemName}"/>
     * <itemDefinition structureRef="{dataType}" id="_{itemName}Item" />
     */
    protected Property addProcessVariable(String itemName, String dataType) {
    	
    	// if it already exists, return the existing property
    	if (properties.get(itemName) != null) {
    		return properties.get(itemName);
    	}
    	
        TItemDefinition itemDef = new TItemDefinition();
        String id = "_" + itemName + "Item";
        itemDef.setId(id);
        itemDef.setStructureRef(new QName(dataType));   
        definitions.getRootElements().add(modelFactory.createItemDefinition(itemDef));
        
        Property prop = new Property();
        prop.setId(itemName);
        prop.setItemSubjectRef(new QName(id));
        getTarget().getProperties().add(prop);  
        properties.put(itemName, prop);
        return prop;
    } 
    
    /*
     * Returns an existing property so we can reuse it
     */
    protected Property getProcessVariable(String name) {
    	return properties.get(name);
    }
	
	public void addImport(String className)  {
		
		if (doc == null) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				doc = dbf.newDocumentBuilder().newDocument();
			}
			catch (Exception e) {
				log.error(e);
				return;
			}			
		}
		
		// only need one import per unique className
		if (!imports.contains(className)) {	
			imports.add(className);
			
			ExtensionElements ee = getTarget().getExtensionElements();
			if (ee == null) {
				ee = new ExtensionElements();
				getTarget().setExtensionElements(ee);
			}

			Element e = doc.createElementNS("http://www.jboss.org/drools", "import");
			e.setAttribute("name", className);
			log.debug(e.getClass().getName());
			ee.getAnies().add(e);
			
		}
		
	}
	
	public Definitions getDefinitions() {
		return definitions;
	}
	
	private void initialize(Process source, TProcess process) {
        definitions = new Definitions();
        definitions.setName(source.getName());
        definitions.setId(DEFINITIONS_ID);  
        definitions.setTargetNamespace(DROOLS_NAMESPACE);
        definitions.setTypeLanguage(JAVA_TYPES_NAMESPACE);
        definitions.setExpressionLanguage(MVEL_NAMESPACE);
               
        process.setProcessType(TProcessType.PRIVATE);
        process.setIsExecutable(Boolean.TRUE);
        process.getOtherAttributes().put(new QName(DROOLS_NAMESPACE,"packageName","tns"), "defaultPackage");
        process.setId(source.getKey());
        process.setName(source.getName());
        
        definitions.getRootElements().add(modelFactory.createProcess(process));
        
        BPMNDiagram diagram = new BPMNDiagram();
        plane = new BPMNPlane();
        plane.setBpmnElement(new QName(getTarget().getId()));
        diagram.setBPMNPlane(plane);
        definitions.getBPMNDiagrams().add(diagram);        
	}
    

}
