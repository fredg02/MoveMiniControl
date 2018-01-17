package microbit.minimove;

/**
 * ConnectionListener
 */

public interface ConnectionListener {

    public void connectionStateChanged(BleConnection.State state);
}