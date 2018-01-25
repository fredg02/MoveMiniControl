package microbit.movemini;

/**
 * ConnectionListener
 */

public interface ConnectionListener {

    public void connectionStateChanged(BleConnection.State state);
}