package com.cts.StatementProcessorDemo.Controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.cts.StatementProcessorDemo.Model.Statement;
import com.cts.StatementProcessorDemo.Model.StatementResult;
import com.cts.StatementProcessorDemo.Service.StatementService;

@RestController
public class StatementController {
    @Autowired
    StatementService statementService;

    @RequestMapping(method = RequestMethod.GET, value = "/processStatement", produces = "text/csv")
    public ResponseEntity<FileSystemResource> processStatement(@RequestParam("file") MultipartFile inputFile) {
        List<Statement> statementList = null;

        // Check for the file type and call corresponding service method to
        // parse the file
        if (inputFile.getOriginalFilename().endsWith(".csv")) {
            statementList = statementService.readCsv(inputFile);
        } else if (inputFile.getOriginalFilename().endsWith(".xml")) {
            statementList = statementService.readXml(inputFile);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File extension should be csv of xml");
        }

        // Filter the list to hold the reference duplicate and balance
        // mismatch records
        List<StatementResult> statementResultsList = statementService.filterOutputRecords(statementList);

        statementService.getResult(statementResultsList);
        File file = new File("ResultFile.csv");
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=ResultFile.csv")
                .contentLength(file.length()).contentType(MediaType.parseMediaType("text/csv"))
                .body(new FileSystemResource(file));

    }
}