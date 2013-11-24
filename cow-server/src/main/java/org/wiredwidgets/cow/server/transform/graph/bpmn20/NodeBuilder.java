package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.wiredwidgets.cow.server.api.model.v2.Activity;

public interface NodeBuilder<T extends Activity> {
	
	public Bpmn20Node build(T activity, Bpmn20ProcessContext context);
	
	public Class<T> getType();

}
