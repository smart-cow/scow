/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2014 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

package amqp.denim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.jivesoftware.smack.XMPPException;

import amqp.AmqpReceiver;
import amqp.denim.DenimConsumer;
import amqp.translators.XmppMessageFromProperties;
import amqp.xmpp.AmqpToXmppGroup;
import amqp.xmpp.AmqpToXmppUser;
import amqp.xmpp.XmppSender;

/**
 * Starts the application that takes messages from AMQP and sends them over XMPP.
 * Takes configuration parameters from command line using the
 * {@code -D<property name>=<property value>} syntax. If property is not in 
 * {@code System.getPropeties()} then $COW_APPS_HOME/cow-server/config/cow-server.properties
 * is checked.
 * 
 * Properties are:
 * <dl>
 * 	<dt>amqp.host</dt>
 * 		<dd> Host name where AMQP is running. Defaults to localhost</dd> 
 * 	<dt>amqp.port</dt>
 * 		<dd>defaults to 5672</dd>
 * 	<dt>amqp.exchange</dt>
 * 		<dd> Name of AMQP exchange to read from. Defaults to amq.topic </dd>
 * 	<dt>amqp.user</dt>
 * 		<dd> defaults to guest</dd>
 * 	<dt>amqp.password<dt>
 * 		<dd> defaults to guest</dd>
 * 
 * 	<dt>xmpp.host</dt>
 * 		<dd> defaults to localhost </dd> 
 * 	<dt>xmpp.notify.user</dt>
 * 		<dd> 
 * 			XMPP username for the user that will send out the AMQP message. 
 * 			Defaults to notifications 
 * 		</dd> 
 * 	<dt>xmpp.notify.password</dt>
 * 		<dd> Password for xmpp.notifier.username. Defaults to password </dd>
 * </dl>
 * 
 * @author brosenberg
 *
 */
public class Driver {

	private static final String AMQP_HOST;
	private static final int AMQP_PORT;
	private static final String AMQP_EXCHANGE;
	private static final String AMQP_USER;
	private static final String AMQP_PASSWORD;
	

	private static Properties extProps_;
	
	
	
	static {
		loadExternalProperties();
		
		AMQP_HOST = getProp("amqp.host", "scout3");
		AMQP_PORT = getIntProp("amqp.port", 5672);
		AMQP_EXCHANGE = getProp("amqp.exchange", "amq.topic");
		AMQP_USER = getProp("amqp.user", "guest");
		AMQP_PASSWORD = getProp("amqp.password", "guest");

	}
	


	private static String getProp(String key, String defaultValue) {
		return System.getProperty(key, extProps_.getProperty(key, defaultValue));
	}
	
	
	private static int getIntProp(String key, int defaultValue) {
		String intString = System.getProperty(key);
		try {
			return Integer.parseInt(intString);
		} catch (NumberFormatException e) { }
		
		intString = extProps_.getProperty(key);
		try {
			return Integer.parseInt(intString);
		} catch (NumberFormatException e) { }
		
		return defaultValue;
	}
	
	
	private static void loadExternalProperties() {
		extProps_ = new Properties();
		
		try {
			FileInputStream in = new FileInputStream(findCowServerProperties());
			extProps_.load(in);
			in.close();
		}
		catch (IOException e) {
			System.err.println("Could not load external properties file");
		}		
	}
	
	
	private static File findCowServerProperties() throws FileNotFoundException {
		String pathFromCowHome = "/cow-server/config/cow-server.properties";
		String cowEnvVar = System.getenv("COW_APPS_HOME");		
		File cowProperties;
		
		if (cowEnvVar != null) {
			cowProperties = new File(cowEnvVar + pathFromCowHome);
			if (cowProperties.exists()) {
				return cowProperties;
			}
		}
		
		for (File root : File.listRoots()) {
			String rootPath = root.getAbsolutePath();
			cowProperties = new File(rootPath + "cow" + pathFromCowHome);
			if (cowProperties.exists()) {
				return cowProperties;
			}
		}
		throw new FileNotFoundException();
	}
	
	
	public static void main(String[] args) throws XMPPException, IOException, InterruptedException 
	{

		System.out.println("Starting Denim Driver");
		AmqpReceiver amqpReceiver = new AmqpReceiver(AMQP_HOST, AMQP_PORT, AMQP_USER, 
				 AMQP_PASSWORD, AMQP_EXCHANGE);
		
		DenimConsumer denimConsumer = new DenimConsumer(amqpReceiver);
		
	}
}
