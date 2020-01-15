package com.cts.StatementProcessorDemo.Model;

public class StatementResult {
    private String referenceNo;
    private String description;

    public StatementResult(String referenceNo, String description) {
        this.referenceNo = referenceNo;
        this.description = description;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
