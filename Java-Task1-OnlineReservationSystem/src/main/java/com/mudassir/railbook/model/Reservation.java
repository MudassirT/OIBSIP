package com.mudassir.railbook.model;

import java.util.Objects;

/**
 * Plain Old Java Object (POJO) representing a train reservation / ticket.
 *
 * @author Mudassir Ahmed
 */
public class Reservation {

    private String pnr;
    private int userId;
    private String passengerName;
    private int trainNumber;
    private String trainName; // populated for display/convenience
    private String classType;
    private String journeyDate; // formatted as yyyy-MM-dd
    private String source;
    private String destination;
    private String bookedAt; // formatted timestamp

    public Reservation() {
    }

    public Reservation(String pnr, int userId, String passengerName, int trainNumber,
                       String classType, String journeyDate, String source,
                       String destination, String bookedAt) {
        this.pnr = pnr;
        this.userId = userId;
        this.passengerName = passengerName;
        this.trainNumber = trainNumber;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.source = source;
        this.destination = destination;
        this.bookedAt = bookedAt;
    }

    public Reservation(String pnr, int userId, String passengerName, int trainNumber,
                       String trainName, String classType, String journeyDate,
                       String source, String destination, String bookedAt) {
        this(pnr, userId, passengerName, trainNumber, classType, journeyDate, source, destination, bookedAt);
        this.trainName = trainName;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
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

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(String bookedAt) {
        this.bookedAt = bookedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(pnr, that.pnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pnr);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "pnr='" + pnr + '\'' +
                ", passengerName='" + passengerName + '\'' +
                ", trainNumber=" + trainNumber +
                ", trainName='" + trainName + '\'' +
                ", classType='" + classType + '\'' +
                ", journeyDate='" + journeyDate + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", bookedAt='" + bookedAt + '\'' +
                '}';
    }
}
