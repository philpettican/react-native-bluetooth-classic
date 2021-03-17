package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Properties;

public class HexStreamDeviceConnectionImpl extends AbstractDeviceConnection {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * The buffer in which data is stored.
     */
    private final StringBuffer mBuffer;

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * Creates a new {@link AbstractDeviceConnection} to the provided NativeDevice, using the provided
     * Properties.
     *
     * @param socket
     * @param properties
     */
    public HexStreamDeviceConnectionImpl(BluetoothSocket socket, Properties properties) throws IOException {
        super(socket, properties);
        this.mBuffer = new StringBuffer();
    }

    @Override
    protected void receivedData(byte[] bytes) {
        Log.d(this.getClass().getSimpleName(),
                String.format("Received %d bytes from device %s", bytes.length, getDevice().getAddress()));

        String hexData = bytesToHex(bytes);
        mBuffer.append(hexData);

        if (mOnDataReceived != null) {
            Log.d(this.getClass().getSimpleName(),
                    "BluetoothEvent.READ listener is registered, providing data");

            mOnDataReceived.accept(getDevice(), read());
        } else {
            Log.d(this.getClass().getSimpleName(),
                    "No BluetoothEvent.READ listeners are registered, skipping handling of the event");
        }
    }

    @Override
    public int available() {
        return mBuffer.length();
    }

    @Override
    public boolean clear() {
        mBuffer.delete(0, mBuffer.length());
        return true;
    }

    @Override
    public String read() {
        String data = mBuffer.toString();
        clear();
        return data;
    }
}
