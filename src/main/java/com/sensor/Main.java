package com.sensor;

import com.sensor.data.SensorData;
import com.sensor.data.ThresholdData;
import com.sensor.error.ErrorHandler;
import com.sensor.processing.DataProcessor;
import com.sensor.processing.OutlierDetector;
import com.sensor.reporting.SensorReportGenerator;
import com.sensor.util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        // Load sensor data and thresholds
        List<SensorData> sensorData = loadSensorData("src/main/resources/sensor_data.csv");
        List<ThresholdData> thresholds = loadThresholds("src/main/resources/thresholds.csv");

        // Process data
        DataProcessor dataProcessor = new DataProcessor();
        Map<String, Map<String, Double>> monthlyAverages = dataProcessor.calculateMonthlyAverages(sensorData);
        Map<String, Map<String, Double>> monthlyMax = dataProcessor.calculateMonthlyMax(sensorData);
        Map<String, Map<String, Double>> monthlyMin = dataProcessor.calculateMonthlyMin(sensorData);

        // Detect outliers
        OutlierDetector outlierDetector = new OutlierDetector();
        List<SensorData> outliers = outlierDetector.detectOutliers(sensorData, thresholds);

        // Generate reports
        SensorReportGenerator reportGenerator = new SensorReportGenerator();
        reportGenerator.generateMonthlyStatsReport(monthlyAverages, monthlyMax, monthlyMin, "monthly_stats.csv");
        reportGenerator.generateOutliersReport(outliers, "outliers.csv");
    }

    private static List<SensorData> loadSensorData(String filePath) {
        List<SensorData> sensorData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int rowNumber = 0;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                try {
                    String[] parts = line.split(",");
                    LocalDateTime date = LocalDateTime.parse(parts[0], formatter);
                    String sensorType = parts[1];
                    double value = Double.parseDouble(parts[2]);
                    String unit = parts[3];
                    String locationId = parts[4];
                    String month = parts[5];
                    sensorData.add(new SensorData(date, sensorType, value, unit, locationId, month));
                } catch (Exception e) {
                    ErrorHandler.handleError(Constants.ERR_CODE_INVALID_FORMAT,
                            "Invalid data format in row " + rowNumber + ": " + line);
                }
            }
        } catch (IOException e) {
            ErrorHandler.handleError(Constants.ERR_CODE_FILE_NOT_FOUND, "File not found: " + filePath);
        }

        return sensorData;
    }

    private static List<ThresholdData> loadThresholds(String filePath) {
        List<ThresholdData> thresholds = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String sensorType = parts[0];
                double minThreshold = Double.parseDouble(parts[1]);
                double maxThreshold = Double.parseDouble(parts[2]);
                thresholds.add(new ThresholdData(sensorType, minThreshold, maxThreshold));
            }
        } catch (IOException e) {
            ErrorHandler.handleError(Constants.ERR_CODE_FILE_NOT_FOUND, "File not found: " + filePath);
        } catch (Exception e) {
            ErrorHandler.handleError(Constants.ERR_CODE_INVALID_FORMAT, "Invalid data format in file: " + filePath);
        }

        return thresholds;
    }
}