package com.cts.StatementProcessorDemo.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cts.StatementProcessorDemo.Model.Statement;
import com.cts.StatementProcessorDemo.Model.StatementResult;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

@Service
public class StatementService {

    public List<Statement> readCsv(MultipartFile inputFile) {

        // Hashmap to map CSV data
        Map<String, String> dataToMap = new HashMap<String, String>();
        dataToMap.put("Reference", "referenceNo");
        dataToMap.put("Description", "description");
        dataToMap.put("Start Balance", "startBalance");
        dataToMap.put("Mutation", "mutation");
        dataToMap.put("End Balance", "endBalance");

        // MappingStrategy to map the data
        HeaderColumnNameTranslateMappingStrategy<Statement> mappingStrategy = new HeaderColumnNameTranslateMappingStrategy<Statement>();
        mappingStrategy.setType(Statement.class);
        mappingStrategy.setColumnMapping(dataToMap);

        // csvReader to read the input file
        CSVReader csvReader = null;
        try {
            Reader reader = new InputStreamReader(inputFile.getInputStream());
            csvReader = new CSVReaderBuilder(reader).build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse and create objects from csv records
        CsvToBean<Statement> csvToBean = new CsvToBean<Statement>();
        List<Statement> statementList = csvToBean.parse(mappingStrategy, csvReader);

        return statementList;
    }

    public List<StatementResult> validateRecords(List<Statement> statementList) {
        List<StatementResult> statementResultsList = new LinkedList<StatementResult>();
        // Map to hold the ReferenceNo and Description
        Map<String, String> referenceMap = new HashMap<String, String>();
        Map<String, Integer> referenceMap1 = new HashMap<String, Integer>();
        for (Statement statement : statementList) {
            Boolean result = validateReferenceNumber(statement, referenceMap1, referenceMap, statementResultsList);
            if (result == false) {
                validateBalance(statement, statementResultsList);
            }
        }
        return statementResultsList;
    }

    private Boolean validateReferenceNumber(Statement statement, Map<String, Integer> referenceMap1,
            Map<String, String> referenceMap, List<StatementResult> statementResultsList) {
        Boolean result = false;
        String referenceNo = statement.getReferenceNo();
        String description = statement.getDescription();
        if (referenceMap.containsKey(referenceNo)) {
            if (referenceMap1.get(referenceNo) == 1) {
                referenceMap1.put(referenceNo, referenceMap1.get(referenceNo) + 1);
                statementResultsList.add(new StatementResult(referenceNo, referenceMap.get(referenceNo)));
            }
            statementResultsList.add(new StatementResult(referenceNo, description));
            result = true;
        } else {
            referenceMap.put(referenceNo, description);
            referenceMap1.put(referenceNo, 1);
            result = false;
        }
        return result;
    }

    private void validateBalance(Statement statement, List<StatementResult> statementResultsList) {
        // Restricting the decimal value to two
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Double startBalance = Double.valueOf(decimalFormat.format(Double.parseDouble(statement.getStartBalance())));
        Double mutation = Double.valueOf(decimalFormat.format(Double.parseDouble(statement.getMutation())));
        Double endBalance = Double.valueOf(decimalFormat.format(Double.parseDouble(statement.getEndBalance())));

        Double balance = Double.valueOf(decimalFormat.format(Double.sum(startBalance, mutation)));
        if (!(Double.compare(balance, endBalance) == 0)) {
            statementResultsList.add(new StatementResult(statement.getReferenceNo(), statement.getDescription()));
        }
    }

    public void getResult(List<StatementResult> statementList)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        // Adding the header to the result file
        statementList.add(new StatementResult("ReferenceNo", "Description"));
        Collections.reverse(statementList);

        FileWriter writer = new FileWriter("ResultFile.csv");
        ColumnPositionMappingStrategy<StatementResult> mappingStrategy = new ColumnPositionMappingStrategy<StatementResult>();
        mappingStrategy.setType(StatementResult.class);

        // Setting the mapping strategy
        String[] columns = new String[] { "ReferenceNo", "Description" };
        mappingStrategy.setColumnMapping(columns);
        StatefulBeanToCsvBuilder<StatementResult> builder = new StatefulBeanToCsvBuilder<StatementResult>(writer);
        StatefulBeanToCsv<StatementResult> beanWriter = builder.withMappingStrategy(mappingStrategy).build();

        beanWriter.write(statementList);
        writer.close();
    }
}
