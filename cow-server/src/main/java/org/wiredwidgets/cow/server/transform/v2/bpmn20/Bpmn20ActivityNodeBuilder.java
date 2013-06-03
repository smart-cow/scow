package org.wiredwidgets.cow.server.transform.v2.bpmn20;


import java.util.List;

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
import org.wiredwidgets.cow.server.api.model.v2.Variable;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;


public abstract class Bpmn20ActivityNodeBuilder<T extends TActivity, U extends Activity> extends Bpmn20FlowNodeBuilder<T, U> {
	
	protected IoSpecification ioSpec = new IoSpecification();
	protected InputSet inputSet = new InputSet();
	protected OutputSet outputSet = new OutputSet();	
	
    public Bpmn20ActivityNodeBuilder(ProcessContext context, T node, U activity) {
        super(context, node, activity);
    }         
    
	protected void addDataInputFromExpression(String name, String value) {     
	    assignInputExpression(addDataInput(name), value);
	}

	protected void addDataInputFromProperty(String name, Property prop) {
		assignInputProperty(addDataInput(name), prop);
	}

	protected void addDataInputFromProperty(String name, String propertyName) {
		assignInputProperty(addDataInput(name), getContext().addProcessVariable(propertyName, "String"));
	}


	/**
	 * Create a new DataOutput linked to a new process level variable
	 * @param name
	 * @param addProcessVar true 
	 * @return
	 */
	protected DataOutput addDataOutputFromProperty(String name, String propertyName) {	
		return addDataOutputFromProperty(name, getContext().addProcessVariable(propertyName, "String"));
	}

	/**
	 * Create a DataOutput linked to a process level property
	 * @param name
	 * @param prop
	 * @return
	 */
	protected DataOutput addDataOutputFromProperty(String name, Property prop) {
	    DataOutput dataOutput = addDataOutput(name);
	    DataOutputAssociation doa = new DataOutputAssociation();
	    getNode().getDataOutputAssociations().add(doa);
	    
	    // This part is not at all obvious. Determined correct approach by unmarshalling sample BPMN2 into XML
	    // and then examining the java objects
	    
	    doa.getSourceReves().add(factory.createTDataAssociationSourceRef(dataOutput));
	    doa.setTargetRef(prop);
	
	    return dataOutput;    	
	}
	
	protected void addInputOutputVariables(List<Variable> vars) {
	
    	for (Variable v : vars) {
    		if (v.getValue() != null) {
    			addDataInputFromExpression(v.getName(), v.getValue());
    		}
    		else {
    			addDataInputFromProperty(v.getName(), v.getName());
    		}
    		if (v.isOutput()) {
    			addDataOutputFromProperty(v.getName(), v.getName());
    		}	
    	}
	}

	/**
	 * Create a data input from an expression value
	 * Follows JBPM naming conventions
	 * @param name
	 * @param value 
	 */
	private void assignInputExpression(DataInput dataInput, String value) {
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

	/**
	 * Assign output to an expression value
	 * @param dataOutput
	 * @param value
	 */
	private void assignOutputExpression(DataOutput dataOutput, String value) {
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

	/**
	 * Assign a data input to a process level property
	 * @param dataInput
	 * @param prop
	 */
	private void assignInputProperty(DataInput dataInput, Property prop) {
	    DataInputAssociation dia = new DataInputAssociation();
	    getNode().getDataInputAssociations().add(dia);
	    dia.setTargetRef(dataInput);     
	    JAXBElement<Object> ref = factory.createTDataAssociationSourceRef(prop);
	    dia.getSourceReves().add(ref);
	} 
	
	private String getInputRefName(String name) {
	    return getNode().getId() + "_" + name + "Input"; // JBPM naming convention
	}
	
	private String getOutputRefName(String name) {
	    return getNode().getId() + "_" + name + "Output"; // JBPM naming convention
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
	private DataInput addDataInput(String name) {
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
	private DataOutput addDataOutput(String name) {
	    DataOutput dataOutput = new DataOutput();
	    dataOutput.setId(getOutputRefName(name));
	    dataOutput.setName(name); 
	    ioSpec.getDataOutputs().add(dataOutput);
	    outputSet.getDataOutputRefs().add(factory.createOutputSetDataOutputRefs(dataOutput));  
	    return dataOutput;
	}	
	
	
}
