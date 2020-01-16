package com.cts.StatementProcessorDemo.Controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cts.StatementProcessorDemo.Model.Statement;
import com.cts.StatementProcessorDemo.Model.StatementResult;
import com.cts.StatementProcessorDemo.Service.StatementService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

@RestController
public class StatementController {
    @Autowired
    StatementService statementService;

    @RequestMapping(method = RequestMethod.GET, value = "/processStatement", produces = "text/csv")
    public ResponseEntity<FileSystemResource> processStatement(@RequestParam("file") MultipartFile inputFile)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException, Exception {
        List<Statement> statementList = null;
        if (inputFile.getOriginalFilename().endsWith(".csv")) {
            statementList = statementService.readCsv(inputFile);
        } else if (inputFile.getOriginalFilename().endsWith(".xml")) {
            statementList = statementService.readXml(inputFile);
        } else {
            throw new Exception("Provide a file of type csv or xml");
        }

        List<StatementResult> statementResultsList = statementService.filterOutputRecords(statementList);

        statementService.getResult(statementResultsList);

        File file = new File("ResultFile.csv");
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=ResultFile.csv")
                .contentLength(file.length()).contentType(MediaType.parseMediaType("text/csv"))
                .body(new FileSystemResource(file));

    }
}