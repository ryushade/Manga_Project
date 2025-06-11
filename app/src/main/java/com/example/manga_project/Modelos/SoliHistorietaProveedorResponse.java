package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SoliHistorietaProveedorResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private List<SoliHistorietaProveedorRequest> data;

    @SerializedName("message")
    private String message;

    public SoliHistorietaProveedorResponse() { }

    public boolean isSuccess() {
        return success;
    }

    public List<SoliHistorietaProveedorRequest> getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setData(List<SoliHistorietaProveedorRequest> data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
