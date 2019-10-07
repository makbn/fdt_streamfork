package ch.ethz.ssh2;

/**
 * A <code>ConnectionMonitor</code> is used to get notified when the
 * underlying socket of a connection is closed.
 *
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id: ConnectionMonitor.java,v 1.3 2006/08/11 17:44:37 cplattne Exp $
 */

public interface ConnectionMonitor {
    /**
     * This method is called after the connection's underlying
     * socket has been closed. E.g., due to the {@link Connection#close()} request of the
     * user, if the peer closed the connection, due to a fatal error during connect()
     * (also if the socket cannot be established) or if a fatal error occured on
     * an established connection.
     * <p>
     * This is an experimental feature.
     * <p>
     * You MUST NOT make any assumption about the thread that invokes this method.
     * <p>
     * <b>Please note: if the connection is not connected (e.g., there was no successful
     * connect() call), then the invocation of {@link Connection#close()} will NOT trigger
     * this method.</b>
     *
     * @param reason Includes an indication why the socket was closed.
     * @see Connection#addConnectionMonitor(ConnectionMonitor)
     */
    public void connectionLost(Throwable reason);
}