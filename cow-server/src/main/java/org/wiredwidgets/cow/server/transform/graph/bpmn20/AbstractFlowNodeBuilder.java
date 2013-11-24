package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

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
import org.omg.spec.bpmn._20100524.model.TCatchEvent;
import org.omg.spec.bpmn._20100524.model.TFlowNode;
import org.omg.spec.bpmn._20100524.model.TFormalExpression;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Variable;

public abstract class AbstractFlowNodeBuilder<T extends Activity, U extends TFlowNode> implements NodeBuilder<T> {
	
	protected static org.omg.spec.bpmn._20100524.model.ObjectFactory factory = new org.omg.spec.bpmn._20100524.model.ObjectFactory();
	
	@Override
	public Bpmn20Node build(T activity, Bpmn20ProcessContext context) {
		U node = newNode();
		setId(node, activity, context);
		buildInternal(node, activity, context);
		return new Bpmn20Node(createElement(node));
	}
	
	/**
	 * Implement details of node building here.  Default implementation
	 * does nothing.
	 * @param node
	 * @param activity
	 * @param context
	 */
	protected void buildInternal(U node, T activity, Bpmn20ProcessContext context) {
		// override where needed
	}

	public abstract U newNode();
	
	public abstract JAXBElement<U> createElement(U node);
	
    private void setId(TFlowNode node, Activity activity, Bpmn20ProcessContext context) {
    	String id = null;
    	// Legacy workflows created before the new ID system will have keys but the
    	// maxID will not be set for the process.  So, we handle those as "new" workflows
    	// the first time they are saved, in order to get the maxID set correctly.
    	if (context.isRevised() && activity != null && activity.getKey() != null  && !activity.getKey().isEmpty()) {
    		id = activity.getKey();
    	}
    	else {
    		id = context.generateId("_");
    	}

    	node.setId(id);
    	
    	if (activity != null) {
    		activity.setKey(id);  	
    	}
    }	
	
	protected final void addDataInputFromExpression(String name, String value, TActivity node) {     
	    assignInputExpression(addDataInput(name, node), value, node);
	}

	protected final void addDataInputFromProperty(String name, Property prop, TActivity node) {
		assignInputProperty(addDataInput(name, node), prop, node);
	}

	protected final void addDataInputFromProperty(String name, String propertyName, TActivity node, Bpmn20ProcessContext context) {
		assignInputProperty(addDataInput(name, node), context.addProcessVariable(propertyName, "String"), node);
	}


	/**
	 * Create a new DataOutput linked to a new process level variable
	 * @param name
	 * @param addProcessVar true 
	 * @return
	 */
	protected final DataOutput addDataOutputFromProperty(String name, String propertyName, TActivity node, Bpmn20ProcessContext context) {	
		return addDataOutputFromProperty(name, context.addProcessVariable(propertyName, "String"), node);
	}

	/**
	 * Create a DataOutput linked to a process level property
	 * @param name
	 * @param prop
	 * @return
	 */
	protected final DataOutput addDataOutputFromProperty(String name, Property prop, TFlowNode node) {
	    DataOutput dataOutput = addDataOutput(name, node);
	    DataOutputAssociation doa = new DataOutputAssociation();
	    getDataOutputAssociations(node).add(doa);
	    
	    // This part is not at all obvious. Determined correct approach by unmarshalling sample BPMN2 into XML
	    // and then examining the java objects
	    
	    doa.getSourceReves().add(factory.createTDataAssociationSourceRef(dataOutput));
	    doa.setTargetRef(prop);
	
	    return dataOutput;    	
	}
	
	protected final void addInputOutputVariables(List<Variable> vars, TActivity node, Bpmn20ProcessContext context) {
	
    	for (Variable v : vars) {
    		if (v.getValue() != null) {
    			addDataInputFromExpression(v.getName(), v.getValue(), node);
    		}
    		else {
    			addDataInputFromProperty(v.getName(), v.getName(), node, context);
    		}
    		if (v.isOutput()) {
    			addDataOutputFromProperty(v.getName(), v.getName(), node, context);
    		}	
    	}
	}
	
    protected final void addOtherAttribute(String name, String value, TFlowNode node) {
        node.getOtherAttributes().put(new QName("http://www.jboss.org/drools", name), value);
    }	

	/**
	 * Create a data input from an expression value
	 * Follows JBPM naming conventions
	 * @param name
	 * @param value 
	 */
	private void assignInputExpression(DataInput dataInput, String value, TActivity node) {
	    DataInputAssociation dia = new DataInputAssociation();
	    getDataInputAssociations(node).add(dia);
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
	private void assignOutputExpression(DataOutput dataOutput, String value, TActivity node) {
	    DataOutputAssociation doa = new DataOutputAssociation();
	    getDataOutputAssociations(node).add(doa);
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
	private void assignInputProperty(DataInput dataInput, Property prop, TActivity node) {
	    DataInputAssociation dia = new DataInputAssociation();
	    node.getDataInputAssociations().add(dia);
	    dia.setTargetRef(dataInput);     
	    JAXBElement<Object> ref = factory.createTDataAssociationSourceRef(prop);
	    dia.getSourceReves().add(ref);
	} 
	
	private String getInputRefName(String name, TFlowNode node) {
	    return node.getId() + "_" + name + "Input"; // JBPM naming convention
	}
	
	private String getOutputRefName(String name, TFlowNode node) {
	    return node.getId() + "_" + name + "Output"; // JBPM naming convention
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
	private DataInput addDataInput(String name, TActivity node) {
	    DataInput dataInput = new DataInput();
	    dataInput.setId(getInputRefName(name, node));
	    dataInput.setName(name); 
	    getIoSpec(node).getDataInputs().add(dataInput);
	    getInputSet(node).getDataInputRefs().add(factory.createInputSetDataInputRefs(dataInput));  
	    return dataInput;
	}
	
	private InputSet getInputSet(TActivity node) {
		if (getIoSpec(node).getInputSets().size() == 0) {
			getIoSpec(node).getInputSets().add(new InputSet());
		}
		return getIoSpec(node).getInputSets().get(0);
	}
	
	private OutputSet getOutputSet(TFlowNode node) {
		if (node instanceof TActivity) {
			IoSpecification ioSpec = getIoSpec((TActivity)node);
			if (ioSpec.getOutputSets().size() == 0) {
				ioSpec.getOutputSets().add(new OutputSet());
			}
			return ioSpec.getOutputSets().get(0);
		}
		else if (node instanceof TCatchEvent) {
			TCatchEvent tce = (TCatchEvent)node;
			if (tce.getOutputSet() == null) {
				tce.setOutputSet(new OutputSet());
			}
			return tce.getOutputSet();
		}
		else {
			throw new RuntimeException("No OutputSet for node type " + node.getClass().getSimpleName());
		}
	}	
	
	/**
	 * Adds a data output item and adds it to the output set
	 * 
	 * 
	 * 
	 * @param name
	 * @return
	 */
	private DataOutput addDataOutput(String name, TFlowNode node) {
	    DataOutput dataOutput = new DataOutput();
	    dataOutput.setId(getOutputRefName(name, node));
	    dataOutput.setName(name); 
	    getDataOutputs(node).add(dataOutput);
	    getOutputSet(node).getDataOutputRefs().add(factory.createOutputSetDataOutputRefs(dataOutput));  
	    return dataOutput;
	}	
	
	private IoSpecification getIoSpec(TActivity node) {
		if (node.getIoSpecification() == null) {
			node.setIoSpecification(new IoSpecification());
		}
		return node.getIoSpecification();
	}
	
	private List<DataOutputAssociation> getDataOutputAssociations(TFlowNode node) {
		if (node instanceof TActivity) {
			return ((TActivity)node).getDataOutputAssociations();
		}
		else if (node instanceof TCatchEvent) {
			return ((TCatchEvent)node).getDataOutputAssociations();
		}
		else {
			throw new RuntimeException("Node type "+ node.getClass().getSimpleName() + " does not have OutputAssociations");
		}
	}
	
	private List<DataInputAssociation> getDataInputAssociations(TFlowNode node) {
		if (node instanceof TActivity) {
			return ((TActivity)node).getDataInputAssociations();
		}
		else {
			throw new RuntimeException("Node type "+ node.getClass().getSimpleName() + " does not have InputAssociations");
		}
	}	
	
	private List<DataOutput> getDataOutputs(TFlowNode node) {
		if (node instanceof TActivity) {
			return getIoSpec((TActivity)node).getDataOutputs();
		}
		else if (node instanceof TCatchEvent) {
			return ((TCatchEvent)node).getDataOutputs();
		}
		else {
			throw new RuntimeException("Node type "+ node.getClass().getSimpleName() + " does not have DataOutputs");
		}		
		
	}
	
}
