package org.wiredwidgets.cow.ac.util.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.jms.*;
import org.apache.log4j.Logger;
import org.apache.qpid.AMQConnectionFailureException;
import org.apache.qpid.AMQException;
import org.apache.qpid.client.AMQConnection;
import org.apache.qpid.client.AMQTopic;
import org.apache.qpid.url.URLSyntaxException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.wiredwidgets.cow.ac.client.ui.StatusBar; // used for providing info to the user via the GUI
import org.wiredwidgets.cow.ac.options.CowSettingsPanel;

/**
 * Notification receiver class that uses AMQP messaging to receive events from
 * the bpm server and then send them to the registered listeners. Uses
 * org.apache.qpid as the engine.
 *
 * Uses the COW preferences to get connection info and watches it for changes.
 *
 * @author MJRUSSELL, modified by RYANMILLER and SFITZPATRICK
 *
 * @see
 * org.wiredwidgets.cow.ac.workflowsummary.notification.ChicletNotificationManager
 * @see org.wiredwidgets.cow.ac.client.server.TaskEventManager
 */
public class BPMNotificationReceiver implements PreferenceChangeListener, MessageListener {

    static final Logger log = Logger.getLogger(BPMNotificationReceiver.class);
    public static final String DESTINATION_NAME = "process";
    private static final String DESTINATION_PORT = "5672";
    private static BPMNotificationReceiver instance = new BPMNotificationReceiver();
    private List<BPMEventListener> bpmEventListeners;
    private String notificationUrl = "";
    private MessageConsumer messageConsumer;
    private Session session;

    private void setConnectionInfoAndEstablishSession() {
        Preferences pref = NbPreferences.forModule(CowSettingsPanel.class);
        String connectionFactoryLoc = "";

        notificationUrl = pref.get("cownotification", "");
        if (notificationUrl.equals("")) {
            log.warn("Notification url was empty or null. Notifications will not be available.");
            return;
        }

        try {
            connectionFactoryLoc = "amqp://guest:guest@clientid/test?brokerlist='tcp://" + notificationUrl + ":" + DESTINATION_PORT + "'";

            Connection connection = new AMQConnection(connectionFactoryLoc);
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new AMQTopic((AMQConnection) connection, DESTINATION_NAME);
            messageConsumer = session.createConsumer(destination);
            messageConsumer.setMessageListener(this);

        } catch (AMQConnectionFailureException ex) {
            log.warn("Incorrect notification server login or server is down. Tried to connect to: "
                    + connectionFactoryLoc);
            StatusBar.getInstance().setStatusText("Cannot reach the COW notification server. Check the settings in the COW options.");
        } catch (AMQException ex) {
            Exceptions.printStackTrace(ex);
        } catch (URLSyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (JMSException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private BPMNotificationReceiver() {

        bpmEventListeners = new ArrayList<BPMEventListener>();

        // listen for updates to the COW properties to adjust for changed settings
        Preferences pref = NbPreferences.forModule(CowSettingsPanel.class);
        pref.addPreferenceChangeListener(this);

        // kick off the listener
        startListener();
    }

    public static BPMNotificationReceiver getInstance() {
        return instance;
    }

    public void startListener() {
        setConnectionInfoAndEstablishSession();
    }

    /**
     * Add a listener for notification events from the server
     *
     * @param listener
     */
    public void addBpmEventListener(BPMEventListener listener) {
        bpmEventListeners.add(listener);
    }

    /**
     * Remove a registered listener
     *
     * @param listener
     */
    public void removeBpmEventListener(BPMEventListener listener) {
        bpmEventListeners.remove(listener);
    }

    /**
     * Stops listening for notifications. Note that changes to the COW
     * properties will re-trigger listening.
     */
    public void stopListening() {
        try {
            if (messageConsumer != null) {
                messageConsumer.close();
            }
        } catch (JMSException ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().equals("cownotification")) {
            log.info("Notification connection settings updated. Recreating sesssion.");
            stopListening();  // close out the old connection
            startListener();  // start a new one which will use the updated properties
        }

    }

    @Override
    public void onMessage(Message msg) {
        if (msg == null || !(msg instanceof TextMessage)) {
            // incorrect message type received or called erronously
            return;
        }
        String message;

        try {
            message = ((TextMessage) msg).getText();
            // System.out.println(" [x] Received " + message);

            // TODO - This special message formatting needs to be more clearly documented
            // or provide a reference to the server code or server-api where it is explained
            String valuePairs[] = message.split(";");
            String eventType = valuePairs[0].split("=")[1];
            String processId = valuePairs[1].split("=")[1];
            if (eventType.equals("ProcessStarted")) {
                for (BPMEventListener list : bpmEventListeners) {
                    list.processStartedEvent(processId);
                }
            } else if (eventType.equals("ProcessDeleted")) {
                // process deleted events will either have an id which is id.ext
                // if a process instance was deleted, or will be just the id if
                // all instances of that process should be deleted.

                if (-1 == processId.indexOf(".")) {
                    // no .ext, so was a wildcard process delete. remove all
                    // matching process instances
                    for (BPMEventListener list : bpmEventListeners) {
                        list.processRemovedEvent(processId);
                    }
                } else {
                    // id.ext, so remove only the matching instance
                    for (BPMEventListener list : bpmEventListeners) {
                        list.processInstanceRemovedEvent(processId);
                    }
                }
            } else {
                String taskId = valuePairs[2].split("=")[1];
                String assignee = valuePairs[3].split("=")[1];
                if (eventType.equals("TaskTaken")) {
                    for (BPMEventListener list : bpmEventListeners) {
                        list.taskAssignedEvent(processId, taskId, assignee);
                    }
                } else if (eventType.equals("TaskCompleted")) {
                    for (BPMEventListener list : bpmEventListeners) {
                        list.taskCompletedEvent(processId, taskId, assignee);
                    }
                } else {
                    log.warn("Received notification message event of unknown type " + eventType);
                }
            }
        } catch (JMSException ex) {
            log.error("Error processing notification message from the server." + ex.getLocalizedMessage());
        }
    }
}