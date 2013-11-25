package org.wiredwidgets.cow.server.transform.graph.builder;

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.transform.graph.ActivityGraph;

/**
 * A default graph builder will be created for each type of Activity
 * that does not require any graph expansion.
 * @author JKRANES
 *
 * @param <T>
 */
public class DefaultGraphBuilder<T extends Activity> extends AbstractGraphBuilder<T> {
	
	private Class<T> type;
	
	public DefaultGraphBuilder(Class<T> type) {
		this.type = type;
	}

	@Override
	protected void buildInternal(T activity, ActivityGraph graph, Process process) {
		// do nothing
	}

	@Override
	public Class<T> getType() {
		return type;
	}

}
