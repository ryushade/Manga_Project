package com.example.manga_project.Modelos;

public class DevolucionResponse {
    private String refund_id;
    private String status;
    private String msg;

    public DevolucionResponse() {}

    public DevolucionResponse(String refund_id, String status, String msg) {
        this.refund_id = refund_id;
        this.status = status;
        this.msg = msg;
    }

    // Getters y Setters
    public String getRefund_id() {
        return refund_id;
    }

    public void setRefund_id(String refund_id) {
        this.refund_id = refund_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
