package org.wiredwidgets.cow.server.transform.v2.bpmn20;


import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.Assignment;
import org.omg.spec.bpmn._20100524.model.DataInput;
import org.omg.spec.bpmn._20100524.model.DataInputAssociation;
import org.omg.spec.bpmn._20100524.model.DataOutput;
import org.omg.spec.bpmn._20100524.model.DataOutputAssociation;
import org.omg.spec.bpmn._20100524.model.InputSet;
import org.omg.spec.bpmn._20100524.model.IoSpecification;
import org.omg.spec.bpmn._20100524.model.OutputSet;
import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TActivity;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;


public abstract class Bpmn20ActivityNodeBuilder<T extends TActivity, U extends Activity> extends Bpmn20FlowNodeBuilder<T, U> {
	
	protected IoSpecification ioSpec = new IoSpecification();
	protected InputSet inputSet = new InputSet();
	protected OutputSet outputSet = new OutputSet();	
	
    public Bpmn20ActivityNodeBuilder(ProcessContext context, T node, U activity) {
        super(context, node, activity);
    }         
    
	private String getInputRefName(String name) {
	    return getNode().getId() + "_" + name + "Input"; // JBPM naming convention
	}
	
	private String getOutputRefName(String name) {
	    return getNode().getId() + "_" + name + "Output"; // JBPM naming convention
	}	

	protected void addDataInput(String name, String value) {     
	    assignInputValue(addDataInput(name), value);
	}

	protected void addDataInput(String name, Property value) {
		assignInputValue(addDataInput(name), value);
	}

	/**
	 * Adds a data input item and adds it to the input set
	 * 
	 * <dataInput name="{name}" id="_{node id}_{name}Input"/>
	 * <inputSet>
	 * 	<dataInputRefs>{dataInput id}</dataInputRefs>
	 * </inputSet>
	 * 
	 * @param name
	 * @return
	 */
	protected DataInput addDataInput(String name) {
	    DataInput dataInput = new DataInput();
	    dataInput.setId(getInputRefName(name));
	    dataInput.setName(name); 
	    ioSpec.getDataInputs().add(dataInput);
	    inputSet.getDataInputRefs().add(factory.createInputSetDataInputRefs(dataInput));  
	    return dataInput;
	}
	
	/**
	 * Adds a data output item and adds it to the output set
	 * 
	 * 
	 * 
	 * @param name
	 * @return
	 */
	protected DataOutput addDataOutput(String name) {
	    DataOutput dataOutput = new DataOutput();
	    dataOutput.setId(getOutputRefName(name));
	    dataOutput.setName(name); 
	    ioSpec.getDataOutputs().add(dataOutput);
	    outputSet.getDataOutputRefs().add(factory.createOutputSetDataOutputRefs(dataOutput));  
	    return dataOutput;
	}	

	/**
	 * Create a new DataOutput linked to a new process level variable
	 * @param name
	 * @param addProcessVar true 
	 * @return
	 */
	protected DataOutput addDataOutput(String name, String processVarName) {	
		return addDataOutput(name, getContext().addProcessVariable(processVarName, "String"));
	}

	protected DataOutput addDataOutput(String name, Property prop) {
	    DataOutput dataOutput = new DataOutput();
	    String id = getNode().getId() + "_" + name + "Output"; // JBPM naming convention
	    dataOutput.setId(id);
	    dataOutput.setName(name); 
	    ioSpec.getDataOutputs().add(dataOutput);
	    outputSet.getDataOutputRefs().add(factory.createOutputSetDataOutputRefs(dataOutput)); 
	    
	    DataOutputAssociation doa = new DataOutputAssociation();
	    getNode().getDataOutputAssociations().add(doa);
	    
	    // This part is not at all obvious. Determined correct approach by unmarshalling sample BPMN2 into XML
	    // and then examining the java objects
	    
	    doa.getSourceReves().add(factory.createTDataAssociationSourceRef(dataOutput));
	    doa.setTargetRef(prop);
	
	    return dataOutput;    	
	}

	/**
	 * Follows JBPM naming conventions
	 * @param name
	 * @param value 
	 */
	protected void assignInputValue(DataInput dataInput, String value) {
	    DataInputAssociation dia = new DataInputAssociation();
	    getNode().getDataInputAssociations().add(dia);
	    dia.setTargetRef(dataInput);
	
	    Assignment assignment = new Assignment();
	    
	    TFormalExpression tfeFrom = new TFormalExpression();
	    tfeFrom.getContent().add(value);
	    assignment.setFrom(tfeFrom);
	          
	    TFormalExpression tfeTo = new TFormalExpression();
	    tfeTo.getContent().add(dataInput.getId());
	    assignment.setTo(tfeTo);
	    
	    dia.getAssignments().add(assignment); 
	}

	protected void assignOutputValue(DataOutput dataOutput, String value) {
	    DataOutputAssociation doa = new DataOutputAssociation();
	    getNode().getDataOutputAssociations().add(doa);
	    doa.setTargetRef(dataOutput);
	
	    Assignment assignment = new Assignment();
	    
	    TFormalExpression tfeFrom = new TFormalExpression();
	    tfeFrom.getContent().add(dataOutput.getId());
	    assignment.setFrom(tfeFrom);
	          
	    TFormalExpression tfeTo = new TFormalExpression();
	    tfeTo.getContent().add(value);
	    assignment.setTo(tfeTo);
	    
	    doa.getAssignments().add(assignment); 
	}

	protected void assignInputValue(DataInput dataInput, Property prop) {
	    DataInputAssociation dia = new DataInputAssociation();
	    getNode().getDataInputAssociations().add(dia);
	    dia.setTargetRef(dataInput);     
	    JAXBElement<Object> ref = factory.createTDataAssociationSourceRef(prop);
	    dia.getSourceReves().add(ref);
	}    
	
	
}
