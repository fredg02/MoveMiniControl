package microbit.movemini;

/**
 * Utilities
 */

public class Utilities {

    public static String getHexString(byte... array) {
        StringBuffer sb = new StringBuffer();
        for (byte b : array) {
            sb.append(String.format("%02X", b));
            sb.append(" ");
        }
        return sb.toString();
    }
}
