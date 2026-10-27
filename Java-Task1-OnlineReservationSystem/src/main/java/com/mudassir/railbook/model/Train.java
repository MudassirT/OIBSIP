package com.mudassir.railbook.model;

import java.util.Objects;

/**
 * Plain Old Java Object (POJO) representing a train route.
 *
 * @author Mudassir Ahmed
 */
public class Train {

    private int trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;

    public Train() {
    }

    public Train(int trainNumber, String trainName, String sourceStation, String destinationStation) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }

    public String getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Train train)) return false;
        return trainNumber == train.trainNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainNumber);
    }

    @Override
    public String toString() {
        return trainNumber + " - " + trainName + " (" + sourceStation + " -> " + destinationStation + ")";
    }
}
