package aero.sita.pts.gsl.tools.mip.model;


public class ConnectionDetails {

    private String connName;
    private String channel;
    private String username;
    private String password;
    private String qm;
    private String queues;
    
    /**
     * @return the connName
     */
    public String getConnName() {
        return connName;
    }
    /**
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    /**
     * @return the qm
     */
    public String getQm() {
        return qm;
    }
    /**
     * @param connName the connName to set
     */
    public void setConnName(String connName) {
        this.connName = connName;
    }
    /**
     * @param channel the channel to set
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }
    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @param qm the qm to set
     */
    public void setQm(String qm) {
        this.qm = qm;
    }
    public String getQueues() {
        return queues;
    }
    public void setQueues(String queues) {
        this.queues = queues;
    }
    @Override
    public String toString() {
        return " ['" + connName + "', " + channel + "', " + username + "', " + password + "', " + qm + "]";
    }
}
