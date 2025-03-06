package com.sensor.processing;

import com.sensor.data.SensorData;

import java.util.*;
import java.util.stream.Collectors;

public class DataProcessor {

    public Map<String, Map<String, Double>> calculateMonthlyAverages(List<SensorData> sensorData) {
        return sensorData.stream()
                .collect(Collectors.groupingBy(
                        SensorData::getSensorType,
                        Collectors.groupingBy(
                                SensorData::getMonth,
                                Collectors.averagingDouble(SensorData::getValue)
                        )
                ));
    }

    public Map<String, Map<String, Double>> calculateMonthlyMax(List<SensorData> sensorData) {
        return sensorData.stream()
                .collect(Collectors.groupingBy(
                        SensorData::getSensorType,
                        Collectors.groupingBy(
                                SensorData::getMonth,
                                Collectors.collectingAndThen(
                                        Collectors.maxBy(Comparator.comparingDouble(SensorData::getValue)),
                                        opt -> opt.map(SensorData::getValue).orElse(0.0)
                                )
                        )
                ));
    }

    public Map<String, Map<String, Double>> calculateMonthlyMin(List<SensorData> sensorData) {
        return sensorData.stream()
                .collect(Collectors.groupingBy(
                        SensorData::getSensorType,
                        Collectors.groupingBy(
                                SensorData::getMonth,
                                Collectors.collectingAndThen(
                                        Collectors.minBy(Comparator.comparingDouble(SensorData::getValue)),
                                        opt -> opt.map(SensorData::getValue).orElse(0.0)
                                )
                        )
                ));
    }
}