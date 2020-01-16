package com.cts.StatementProcessorDemo.Model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "records")
public class Records {
    private List<Statement> recordList = new LinkedList<Statement>();

    public Records() {
    }

    Records(List<Statement> recordList) {
        this.recordList = recordList;
    }

    @XmlElement(name = "record")
    public List<Statement> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<Statement> recordList) {
        this.recordList = recordList;
    }
}
