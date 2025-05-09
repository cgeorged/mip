package aero.sita.pts.gsl.tools.mip.model;


public class MessageReq {

    private String queue;
    private String body;
    private String header;
    private String type;
    private String qm;
    /**
     * @return the queue
     */
    public String getQueue() {
        return queue;
    }
    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }
    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @return the qm
     */
    public String getQm() {
        return qm;
    }
    /**
     * @param queue the queue to set
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }
    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }
    /**
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @param qm the qm to set
     */
    public void setQm(String qm) {
        this.qm = qm;
    }
    @Override
    public String toString() {
        return " ['" + queue + "', " + body + "', " + header + "', " + type + "', " + qm + "]";
    }
}
