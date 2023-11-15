package com.example.mqttgasalert;

public class Gas {
    int id;
    String value;
    String dateTime;

    public Gas(){

    }

    public Gas(String value, String dateTime) {
        this.value = value;
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}