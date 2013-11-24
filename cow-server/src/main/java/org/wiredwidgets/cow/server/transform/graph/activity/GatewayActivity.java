package org.wiredwidgets.cow.server.transform.graph.activity;

import org.wiredwidgets.cow.server.api.model.v2.Activity;

public abstract class GatewayActivity extends Activity {
	
	public static String DIVERGING = "Diverging";
	public static String CONVERGING = "Converging";
	
	private String direction;
	
	public String getDirection() {
		return direction;
	}
	
	public void setDirection(String direction) {
		this.direction = direction;
	}

}
