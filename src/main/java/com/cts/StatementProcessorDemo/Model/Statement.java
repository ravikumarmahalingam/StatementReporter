package com.cts.StatementProcessorDemo.Model;

public class Statement {
    private String referenceNo;
    private String description;
    private String startBalance;
    private String mutation;
    private String endBalance;

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

    public String getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(String startBalance) {
        this.startBalance = startBalance;
    }

    public String getMutation() {
        return mutation;
    }

    public void setMutation(String mutation) {
        this.mutation = mutation;
    }

    public String getEndBalance() {
        return endBalance;
    }

    public void setEndBalance(String endBalance) {
        this.endBalance = endBalance;
    }

    @Override
    public String toString() {
        return "Statement [ReferenceNo=" + referenceNo + ", Description=" + description + ", StatementBalance="
                + startBalance + ", mutation=" + mutation + ", endBalance=" + endBalance;
        
    }

}
