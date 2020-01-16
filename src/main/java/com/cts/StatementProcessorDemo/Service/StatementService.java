package com.cts.StatementProcessorDemo.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cts.StatementProcessorDemo.Model.Records;
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

        Map<String, String> dataToMap = new HashMap<String, String>();
        dataToMap.put("Reference", "reference");
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
    
    public List<Statement> readXml(MultipartFile inputFile) throws JAXBException, IllegalStateException, IOException {

        File xmlFile = new File(System.getProperty("java.io.tmpdir") + "/" + inputFile);
        inputFile.transferTo(xmlFile);

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(Records.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            Records records = (Records) jaxbUnmarshaller.unmarshal(xmlFile);

            List<Statement> statementList = records.getRecordList();

            return statementList;

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getResult(List<StatementResult> statementList)
            throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        // Adding the header to the result file
        statementList.add(new StatementResult("Reference", "Description"));
        Collections.reverse(statementList);

        FileWriter writer = new FileWriter("ResultFile.csv");
        ColumnPositionMappingStrategy<StatementResult> mappingStrategy = new ColumnPositionMappingStrategy<StatementResult>();
        mappingStrategy.setType(StatementResult.class);

        // Setting the mapping strategy
        String[] columns = new String[] { "Reference", "Description" };
        mappingStrategy.setColumnMapping(columns);
        StatefulBeanToCsvBuilder<StatementResult> builder = new StatefulBeanToCsvBuilder<StatementResult>(writer);
        StatefulBeanToCsv<StatementResult> beanWriter = builder.withMappingStrategy(mappingStrategy).build();

        beanWriter.write(statementList);
        writer.close();
    }



    public List<StatementResult> filterOutputRecords(List<Statement> statementList) {
        List<StatementResult> finalResult = new LinkedList<StatementResult>();
        Map<String, List<Statement>> statementRefGrp = statementList.stream()
                .collect(Collectors.groupingBy(Statement::getReference));

        List<Statement> dupRefStatementList = statementRefGrp.values().stream()
                .filter(duplicates -> duplicates.size() > 1).flatMap(Collection::stream).collect(Collectors.toList());

        statementList.removeAll(dupRefStatementList);

        dupRefStatementList.stream().forEach(statement -> finalResult
                .add(new StatementResult(statement.getReference(), statement.getDescription())));

        List<Statement> balMismatchStatementList = statementList.stream().filter(statement -> checkBalance(statement))
                .collect(Collectors.toList());

        balMismatchStatementList.stream().forEach(statement -> finalResult
                .add(new StatementResult(statement.getReference(), statement.getDescription())));

        return finalResult;
    }

    private Boolean checkBalance(Statement statement) {
        Boolean result = false;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        Double startBalance = Double.valueOf(decimalFormat.format(Double.parseDouble(statement.getStartBalance())));
        Double mutation = Double.valueOf(decimalFormat.format(Double.parseDouble(statement.getMutation())));
        Double endBalance = Double.valueOf(decimalFormat.format(Double.parseDouble(statement.getEndBalance())));

        Double balance = Double.valueOf(decimalFormat.format(Double.sum(startBalance, mutation)));
        if (!(Double.compare(balance, endBalance) == 0)) {
            result = true;
        }
        return result;
    }
}
