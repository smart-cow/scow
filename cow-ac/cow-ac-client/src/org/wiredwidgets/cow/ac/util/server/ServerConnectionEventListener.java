package org.wiredwidgets.cow.ac.util.server;

/**
 * Allows for callbacks when the connection to the server has been changed.
 *
 * @author RYANMILLER
 */
public interface ServerConnectionEventListener {

    /**
     * Called when the connection to the server has been updated. This can be
     * used to trigger a GUI refresh, for example. Since a refresh might be an
     * intensive operation, consider spawning a thread to do the additional work
     * to avoid slowing up other notifications.
     * <p/>
     * TODO: could improve by adding a parameter or splitting into different
     * methods to notify if the user/password only changed, or if the server
     * address changed
     */
    void serverUpdated();

    /**
     * Called when the connection to the server has been changed, but the
     * connection settings are bad or there is a problem with the server.
     */
    void serverDown();
}
