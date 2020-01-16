package com.cts.StatementProcessorDemo.Model;

public class StatementResult {
    private String reference;
    private String description;

    public StatementResult(String reference, String description) {
        this.reference = reference;
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
