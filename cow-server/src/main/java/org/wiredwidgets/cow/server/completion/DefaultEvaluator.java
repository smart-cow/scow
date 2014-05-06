package org.wiredwidgets.cow.server.completion;

import org.wiredwidgets.cow.server.api.model.v2.Activity;

public class DefaultEvaluator<T extends Activity> extends AbstractEvaluator<T> {
	
	private Class<T> activityClass;
	
	public DefaultEvaluator(Class<T> activityClass) {
		this.activityClass = activityClass;
	}

	@Override
	protected Class<T> getActivityClass() {
		return activityClass;
	}

	@Override
	protected void evaluateInternal() {
		// do nothing
	}

}
