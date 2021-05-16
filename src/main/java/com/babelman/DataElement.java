package com.babelman;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DataElement {
    private LocalDate date;
    private double openTemp, closeTemp, highTemp, lowTemp;
    private String alarm;

    public DataElement(LocalDate date, double openTemp, double closeTemp, double highTemp, double lowTemp, boolean alarm) {
        this.date = date;
        this.openTemp = openTemp;
        this.closeTemp = closeTemp;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.alarm = alarm ? "Triggered" : "Not triggered";
    }

    public String getDate() {
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyy"));
    }

    public String getDayOfWeek() {
        String dow = date.getDayOfWeek().toString();
        return dow.substring(0,1).concat(dow.substring(1,3).toLowerCase());
    }

    public double getOpenTemp() {
        return openTemp;
    }

    public double getCloseTemp() {
        return closeTemp;
    }

    public double getHighTemp() {
        return highTemp;
    }

    public double getLowTemp() {
        return lowTemp;
    }

    public String getAlarmStatus() {
        return alarm;
    }
}
