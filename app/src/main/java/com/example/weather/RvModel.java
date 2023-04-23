package com.example.weather;


public class RvModel {

    private String temperature;
    private String time;
    private String icon;
    private String windSpeed;

    public RvModel(String temperature, String time, String icon, String windSpeed) {
        this.temperature = temperature;
        this.time = time;
        this.icon = icon;
        this.windSpeed = windSpeed;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

}
