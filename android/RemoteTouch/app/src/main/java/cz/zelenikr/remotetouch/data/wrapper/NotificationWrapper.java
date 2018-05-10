package cz.zelenikr.remotetouch.data.wrapper;

/**
 * Simplified notification POJO class.
 *
 * @author Roman Zelenik
 */
public class NotificationWrapper {
    /**
     * id in database
     */
    private long id;
    /**
     * package name of application
     */
    private String application;
    /**
     * timestamp of notification handling
     */
    private long timestamp;

    public NotificationWrapper() {
    }

    public NotificationWrapper(String application, long timestamp) {
        this.application = application;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NotificationWrapper{");
        sb.append("id=").append(id);
        sb.append(", application='").append(application).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}
