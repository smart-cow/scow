package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.Property;
import org.omg.spec.bpmn._20100524.model.TTask;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.ServiceTask;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20UserTaskNodeBuilder;

@Component
public class ServiceTaskNodeBuilder extends AbstractFlowNodeBuilder<ServiceTask, TTask> {

	@Override
	public Class<ServiceTask> getType() {
		return ServiceTask.class;
	}

	@Override
	public TTask newNode() {
		return new TTask();
	}

	@Override
	public JAXBElement<TTask> createElement(TTask node) {
		return factory.createTask(node);
	}

	@Override
	protected void buildInternal(TTask t, ServiceTask st,
			Bpmn20ProcessContext context) {

        t.setName(st.getName());
   
        // this is the name JBPM uses to assign a work item handler
        addOtherAttribute("taskName", "RestService", t);
        
        Property varsProperty = context.getProcessVariable(Bpmn20ProcessBuilder.VARIABLES_PROPERTY);
        addDataInputFromProperty(Bpmn20UserTaskNodeBuilder.TASK_INPUT_VARIABLES_NAME, varsProperty, t);
        addDataOutputFromProperty(Bpmn20UserTaskNodeBuilder.TASK_OUTPUT_VARIABLES_NAME, varsProperty, t);
        
        addDataInputFromExpression("method", st.getMethod(), t);
        addDataInputFromExpression("url", st.getUrl(), t);
        addDataInputFromExpression("content", st.getContent(), t);
        addDataInputFromExpression("var", st.getVar(), t);	
	}
	
	

}
