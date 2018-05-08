package com.github.pires.obd.reader.entity;

/**
 * Created by mathiaseloi on 08/05/18.
 */

public class EntityTripFuel {

    private long timeStamp;
    private double inputFuel;
    private double tankCapacity;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getInputFuel() {
        return inputFuel;
    }

    public void setInputFuel(double inputFuel) {
        this.inputFuel = inputFuel;
    }

    public double getTankCapacity() {
        return tankCapacity;
    }

    public void setTankCapacity(double tankCapacity) {
        this.tankCapacity = tankCapacity;
    }
}
