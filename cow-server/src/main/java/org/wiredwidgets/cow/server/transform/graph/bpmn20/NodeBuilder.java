package org.wiredwidgets.cow.server.transform.graph.bpmn20;

import org.wiredwidgets.cow.server.api.model.v2.Activity;

public interface NodeBuilder {
	
	public Bpmn20Node build(Activity activity, Bpmn20ProcessContext context);
	
	public Class<? extends Activity> getType();

}
