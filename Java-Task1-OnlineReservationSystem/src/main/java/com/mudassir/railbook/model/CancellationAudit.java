package com.mudassir.railbook.model;

/**
 * POJO representing an audit log entry when a reservation is cancelled.
 *
 * @author Mudassir Ahmed
 */
public class CancellationAudit {

    private int id;
    private String pnr;
    private int cancelledBy;
    private String cancelledAt;

    public CancellationAudit() {
    }

    public CancellationAudit(int id, String pnr, int cancelledBy, String cancelledAt) {
        this.id = id;
        this.pnr = pnr;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = cancelledAt;
    }

    public CancellationAudit(String pnr, int cancelledBy, String cancelledAt) {
        this(0, pnr, cancelledBy, cancelledAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public int getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(int cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
