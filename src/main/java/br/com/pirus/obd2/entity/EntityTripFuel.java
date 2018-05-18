package br.com.pirus.obd2.entity;

/**
 * Created by mathiaseloi on 08/05/18.
 */

public class EntityTripFuel {

    private long time;
    private long percentBegin;
    private long percentEnd;
    private long liters;

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public long getPercentBegin() {
        return percentBegin;
    }

    public void setLiters(long liters) {
        this.liters = liters;
    }

    public void setPercentBegin(long percentBegin) {
        this.percentBegin = percentBegin;
    }

    public long getPercentEnd() {
        return percentEnd;
    }

    public void setPercentEnd(long percentEnd) {
        this.percentEnd = percentEnd;
    }

    public long getLiters() {
        return liters;
    }
}