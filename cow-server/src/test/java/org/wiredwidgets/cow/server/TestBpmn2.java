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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server;

import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.spec.bpmn._20100524.model.Assignment;
import org.omg.spec.bpmn._20100524.model.DataInput;
import org.omg.spec.bpmn._20100524.model.DataInputAssociation;
import org.omg.spec.bpmn._20100524.model.DataOutput;
import org.omg.spec.bpmn._20100524.model.DataOutputAssociation;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.omg.spec.bpmn._20100524.model.ExtensionElements;
import org.omg.spec.bpmn._20100524.model.InputSet;
import org.omg.spec.bpmn._20100524.model.IoSpecification;
import org.omg.spec.bpmn._20100524.model.ObjectFactory;
import org.omg.spec.bpmn._20100524.model.OutputSet;
import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.ResourceAssignmentExpression;
import org.omg.spec.bpmn._20100524.model.TDataAssociation;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.omg.spec.bpmn._20100524.model.TItemDefinition;
import org.omg.spec.bpmn._20100524.model.TPotentialOwner;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.TProcessType;
import org.omg.spec.bpmn._20100524.model.TUserTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author JKRANES
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/jaxb-test-context.xml")
public class TestBpmn2 {


    @Autowired
    Jaxb2Marshaller marshaller;
    
    @Test
    public void testUnmarshal() throws Exception {
        
        ObjectFactory factory = new ObjectFactory();
        Definitions definitions = new Definitions();
        definitions.setName("test");
        definitions.setId("Definition");
        definitions.setTargetNamespace("http://www.jboss.org/drools");
        definitions.setTypeLanguage("http://www.java.com/javaTypes");
        definitions.setExpressionLanguage("http://www.mvel.org/2.0");
        
        // itemDefinition
        TItemDefinition itemDef = factory.createTItemDefinition();
        String varName = "var1";
        String id = "_" + varName + "Item";
        itemDef.setId(id);
        definitions.getRootElements().add(factory.createItemDefinition(itemDef));
        
        // process
        TProcess process = new TProcess();
        process.setProcessType(TProcessType.PRIVATE);
        process.setIsExecutable(Boolean.TRUE);
        process.getOtherAttributes().put(new QName("http://www.jboss.org/drools","packageName","tns"), "defaultPackage");
        process.setId("0");
        process.setName("test"); 
        
        // imports
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = dbf.newDocumentBuilder().newDocument();
		Element e = doc.createElementNS("http://www.jboss.org/drools", "import");
		e.setAttribute("name", "java.util.Map");
		
		ExtensionElements ee = new ExtensionElements();
		ee.getAnies().add(e);
		process.setExtensionElements(ee);

        // properties (i.e. process level variables)
        Property prop = new Property();
        prop.setId(varName);
        prop.setItemSubjectRef(new QName(id));
        process.getProperties().add(prop);       
     
        definitions.getRootElements().add(factory.createProcess(process));
        
        // user task
        
        TUserTask userTask = new TUserTask();
        userTask.setId("_1");
        userTask.setName("User Task");

        // Potential Owner
        TFormalExpression formalExpr = new TFormalExpression();
        formalExpr.getContent().add("Joe");
        ResourceAssignmentExpression resourceExpr = new ResourceAssignmentExpression();
        resourceExpr.setExpression(factory.createFormalExpression(formalExpr));
        TPotentialOwner owner = new TPotentialOwner();
        owner.setResourceAssignmentExpression(resourceExpr);
        userTask.getResourceRoles().add(factory.createPotentialOwner(owner));

        // IO Specification
        IoSpecification ioSpec = new IoSpecification();
        DataInput dataInput = new DataInput();
        String inputVarName = "MyVariable";
        String optionsInputRefString = userTask.getId() + "_" + inputVarName + "Input";
        dataInput.setId(optionsInputRefString);
        dataInput.setName(inputVarName);
        ioSpec.getDataInputs().add(dataInput);
        
        // input 2
        DataInput dataInput2 = new DataInput();
        String optionsInputRefString2 = userTask.getId() + "_" + "content" + "Input2";
        dataInput2.setId(optionsInputRefString2);
        dataInput2.setName("input2_name");
        ioSpec.getDataInputs().add(dataInput2);
        
        
        // data output
        DataOutput dataOutput = new DataOutput();
        dataOutput.setId("_x_varOutput");
        dataOutput.setName("var");
        ioSpec.getDataOutputs().add(dataOutput);

        // inputSet
        InputSet inputSet = new InputSet();
        JAXBElement<Object> optionsInputRef = factory.createInputSetDataInputRefs(dataInput);
        inputSet.getDataInputRefs().add(optionsInputRef);
        ioSpec.getInputSets().add(inputSet);
        
        // outputSet
        OutputSet outputSet = new OutputSet();
        
        JAXBElement<Object> ref = new JAXBElement<Object>(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL","sourceRef"), Object.class, TDataAssociation.class, dataOutput);
 
        outputSet.getDataOutputRefs().add(ref);
        ioSpec.getOutputSets().add(outputSet);
        

        // dataInputAssociation
        DataInputAssociation dataInputAssoc = new DataInputAssociation();
        // dataInputAssoc.setTargetRef(optionsInputRef);
        dataInputAssoc.setTargetRef(dataInput);
        Assignment assignment = factory.createAssignment();

        TFormalExpression tfeSource = new TFormalExpression();
        // same as varName from process variables
        tfeSource.getContent().add(varName);

        TFormalExpression tfeTarget = new TFormalExpression();
        tfeTarget.getContent().add(dataInput.getId());

        assignment.setFrom(tfeSource);
        assignment.setTo(tfeTarget);

        dataInputAssoc.getAssignments().add(assignment);
        userTask.setIoSpecification(ioSpec);
        userTask.getDataInputAssociations().add(dataInputAssoc);  
        
        // dataInputAssociation 2
        DataInputAssociation dataInputAssoc2 = new DataInputAssociation();
        dataInputAssoc2.setTargetRef(dataInput2);
        JAXBElement<Object> ref2 = new JAXBElement<Object>(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL","sourceRef"), Object.class, TDataAssociation.class, prop);
        dataInputAssoc2.getSourceReves().add(ref2);
        userTask.getDataInputAssociations().add(dataInputAssoc2);  
              
        // data output assoc
        DataOutputAssociation doa = new DataOutputAssociation();      
        userTask.getDataOutputAssociations().add(doa);
        doa.setTargetRef(prop);
        doa.getSourceReves().add(outputSet.getDataOutputRefs().get(0));
        
        // outgoing
        userTask.getOutgoings().add(new QName("http://www.omg.org/spec/BPMN/20100524/MODEL","xxx"));
        
        
        process.getFlowElements().add(factory.createUserTask(userTask));
        
        
        Map<String, Object> props = new HashMap<String, Object>();
        // props.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // props.put(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd");
        // marshaller.setMarshallerProperties(props);
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(definitions, new StreamResult(sw));
        String str = sw.toString();
        
        assertTrue(true);
        
        
  
    }

    @Test
    public void testMarshal() throws Exception {
        ClassPathResource cpr = new ClassPathResource("import2.bpmn");
        Object defs = marshaller.unmarshal(new StreamSource(cpr.getInputStream()));
        assertTrue(true);
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(defs, new StreamResult(sw));
        String str = sw.toString();
        assertTrue(true);
        
        
    }
}
