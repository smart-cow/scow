package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.model.TCallActivity;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.SubProcess;

@Component
public class SubProcessNodeBuilder extends AbstractFlowNodeBuilder<SubProcess, TCallActivity> {

	@Override
	public Class<SubProcess> getType() {
		return SubProcess.class;
	}

	@Override
	public TCallActivity newNode() {
		return new TCallActivity();
	}

	@Override
	public JAXBElement<TCallActivity> createElement(TCallActivity node) {
		return factory.createCallActivity(node);
	}

	@Override
	protected void buildInternal(TCallActivity t, SubProcess source,
			Bpmn20ProcessContext context) {

        t.setName(source.getName());

        // the process ID of the called process
        t.setCalledElement(new QName(source.getSubProcessKey()));       
        
        // this means that if the parent process is terminated the subprocess will also be terminated.
        addOtherAttribute("independent","false", t);
        
        // inputs and outputs 
        // other variable inputs
        if (source.getVariables() != null) {
        	addInputOutputVariables(source.getVariables().getVariables(), t, context);
        }		
		
		
	}
	
	

}
