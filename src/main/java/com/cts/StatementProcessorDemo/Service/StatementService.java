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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * @author rmahalingam
 */
@Service
public class StatementService {

    Logger logger = LoggerFactory.getLogger(StatementService.class);

    /**
     * This method is to read the given csv file and give the list of
     * StatementRecords as output
     * 
     * @param inputFile
     *            Input csv file which has the data
     * @return List<Statement> List of statements which would be returned by
     *         parsing the csv file
     */
    public List<Statement> readCsv(MultipartFile inputFile) {

        List<Statement> statementList = null;
        try {
            // MappingStrategy to map the data
            Map<String, String> dataToMap = new HashMap<String, String>();
            dataToMap.put("Reference", "reference");
            dataToMap.put("Description", "description");
            dataToMap.put("Start Balance", "startBalance");
            dataToMap.put("Mutation", "mutation");
            dataToMap.put("End Balance", "endBalance");

            HeaderColumnNameTranslateMappingStrategy<Statement> mappingStrategy = new HeaderColumnNameTranslateMappingStrategy<Statement>();
            mappingStrategy.setType(Statement.class);
            mappingStrategy.setColumnMapping(dataToMap);

            // csvReader to read the input file
            CSVReader csvReader = null;
            Reader reader = new InputStreamReader(inputFile.getInputStream());
            csvReader = new CSVReaderBuilder(reader).build();

            // Parse and create objects from csv records
            CsvToBean<Statement> csvToBean = new CsvToBean<Statement>();
            statementList = csvToBean.parse(mappingStrategy, csvReader);

        } catch (IOException e) {
            logger.error("IOException occured while reading the csv file:::" + e.getMessage());
        }
        return statementList;
    }

    /**
     * This method is to read the given xml file and give the list of
     * StatementRecords as output
     * 
     * @param inputFile
     *            Input xml file which has the data
     * @return List<Statement> List of statements which would be returned by
     *         parsing the xml file
     */
    public List<Statement> readXml(MultipartFile inputFile) {
        List<Statement> statementList = null;

        try {
            File xmlFile = new File(System.getProperty("java.io.tmpdir") + "/" + inputFile);
            inputFile.transferTo(xmlFile);

            JAXBContext jaxbContext = JAXBContext.newInstance(Records.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Unmarshal the file and get the records
            Records records = (Records) jaxbUnmarshaller.unmarshal(xmlFile);

            statementList = records.getRecordList();
        } catch (IOException e) {
            logger.error("IOException occured while reading the xml file:::" + e.getMessage());
        } catch (JAXBException e) {
            logger.error("JAXBException occured while reading the xml file:::" + e.getMessage());
        }

        return statementList;
    }

    /**
     * This method is to create the output file with the records having
     * duplicate reference no and balance mismatch
     * 
     * @param statementList
     *            List of records with duplicate reference no and balance
     *            mismatch
     */
    public void getResult(List<StatementResult> statementList) {
        try {
            statementList.add(new StatementResult("Reference", "Description"));
            Collections.reverse(statementList);

            // Mapping strategy for the result file
            FileWriter writer = new FileWriter("ResultFile.csv");
            ColumnPositionMappingStrategy<StatementResult> mappingStrategy = new ColumnPositionMappingStrategy<StatementResult>();
            mappingStrategy.setType(StatementResult.class);

            String[] columns = new String[] { "Reference", "Description" };
            mappingStrategy.setColumnMapping(columns);
            StatefulBeanToCsvBuilder<StatementResult> builder = new StatefulBeanToCsvBuilder<StatementResult>(writer);
            StatefulBeanToCsv<StatementResult> beanWriter = builder.withMappingStrategy(mappingStrategy).build();

            beanWriter.write(statementList);
            writer.close();
        } catch (IOException e) {
            logger.error("IOException occured while reading the xml file:::" + e.getMessage());
        } catch (CsvDataTypeMismatchException e) {
            logger.error("CsvDataTypeMismatchException occured while reading the xml file:::" + e.getMessage());
        } catch (CsvRequiredFieldEmptyException e) {
            logger.error("CsvRequiredFieldEmptyException occured while reading the xml file:::" + e.getMessage());
        }
    }

    /**
     * This method is to filter the records with duplicate reference number and
     * balance mismatch.
     * 
     * @param statementList
     *            InputList which has all the records which came from the input
     *            file
     * @returnList<StatementResult> Output list which has the duplicate
     *                              reference number records and balance
     *                              mismatch records
     */
    public List<StatementResult> filterOutputRecords(List<Statement> statementList) {
        // Output list to hold the duplicate reference records and balance
        // mismatch records
        List<StatementResult> finalResult = new LinkedList<StatementResult>();

        // Group the records with the same reference number
        Map<String, List<Statement>> statementRefGrp = statementList.stream()
                .collect(Collectors.groupingBy(Statement::getReference));

        // List to hold the statement records with same reference number
        List<Statement> dupRefStatementList = statementRefGrp.values().stream()
                .filter(duplicates -> duplicates.size() > 1).flatMap(Collection::stream).collect(Collectors.toList());

        // Remove the duplicate records from the original list
        statementList.removeAll(dupRefStatementList);

        // For each duplicate reference record create a corresponding output
        // record
        dupRefStatementList.stream().forEach(statement -> finalResult
                .add(new StatementResult(statement.getReference(), statement.getDescription())));

        // For the remaining records check for the balance mismatch
        List<Statement> balMismatchStatementList = statementList.stream().filter(statement -> checkBalance(statement))
                .collect(Collectors.toList());

        // For each balance mismatch record create a corresponding output record
        balMismatchStatementList.stream().forEach(statement -> finalResult
                .add(new StatementResult(statement.getReference(), statement.getDescription())));

        return finalResult;
    }

    /**
     * This method is a utility method to check whether the record is with
     * balance mismatch or not
     * 
     * @param statement
     *            Input which has to be validated for balance mismatch
     * @return Boolean Returns true if the record has balance mismatch
     */
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
