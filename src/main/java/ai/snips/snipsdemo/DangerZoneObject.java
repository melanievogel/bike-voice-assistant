package ai.snips.snipsdemo;

import java.io.Serializable;

public class DangerZoneObject implements Serializable{

    private String name;
    private Double longi;
    private Double lati;
    private String distance;

    public DangerZoneObject(String name, Double longi, Double lati, String distance) {

        this.name = name;
        this.longi = longi;
        this.lati = lati;
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongi() {
        return longi;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }

    public Double getLati() {
        return lati;
    }

    public void setLati(Double lati) {
        this.lati = lati;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}