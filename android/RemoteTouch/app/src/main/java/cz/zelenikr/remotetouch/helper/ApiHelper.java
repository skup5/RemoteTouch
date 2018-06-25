package cz.zelenikr.remotetouch.helper;

/**
 * This helper class simplifies checking Api level of the current device.
 *
 * @author Roman Zelenik
 */
public final class ApiHelper {

    public static final int CURRENT_API_LEVEL = android.os.Build.VERSION.SDK_INT;

    /**
     * Checks if api level on this device is equal or greater then {@code requiredApiLevel}.
     *
     * @param requiredApiLevel api level numeric code
     * @return
     */
    public static boolean checkCurrentApiLevel(int requiredApiLevel) {
        if (requiredApiLevel < 1)
            throw new IllegalArgumentException("Api level cannot be less then 1");
        return CURRENT_API_LEVEL >= requiredApiLevel;
    }

    private ApiHelper() {
    }
}
