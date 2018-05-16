package com.github.pires.obd.reader.entity;

/**
 * Created by mathiaseloi on 08/05/18.
 */

public class EntityTripFuel {

    private long time;
    private long percent;
    private long liters;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getPercent() {
        return percent;
    }

    public void setPercent(long percent) {
        this.percent = percent;
    }

    public long getLiters() {
        return liters;
    }

    public void setLiters(long liters) {
        this.liters = liters;
    }
}
