package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import javax.xml.bind.JAXBElement;

import org.omg.spec.bpmn._20100524.model.Script;
import org.omg.spec.bpmn._20100524.model.TScriptTask;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Exit;

@Component
public class ExitNodeBuilder extends AbstractFlowNodeBuilder<Exit, TScriptTask> {

	@Override
	public Class<Exit> getType() {
		return Exit.class;
	}

	@Override
	public TScriptTask newNode() {
		return new TScriptTask();
	}

	@Override
	public JAXBElement<TScriptTask> createElement(TScriptTask node) {
		return factory.createScriptTask(node);
	}

	@Override
	protected void buildInternal(TScriptTask node, Exit exit,
			Bpmn20ProcessContext context) {
		
        node.setName("exit " + exit.getState());
        
        Script script = new Script();         
        String scriptText = "kcontext.getKnowledgeRuntime().signalEvent(\"exit\", \"" 
       		 + exit.getState() + "\", kcontext.getProcessInstance().getId());";   
        script.getContent().add(scriptText);
        node.setScript(script);		
		
	}
	
	
	

}
