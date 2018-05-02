package com.github.pires.obd.reader.util;


import java.util.List;

/**
 *
 */
public class CalcOBD2 {

    /**
     * @param fuel
     * @param massAirFlow
     * @param vehicleSpeed
     * @return
     */
    public static double getFuelConsumption(Fuel fuel, double massAirFlow, int vehicleSpeed) {

        if (massAirFlow == 0.0)
            return 0.0;

        if (vehicleSpeed < 1)
            vehicleSpeed = 1;

        // measured in km/l
        return (fuel.getAirFuel() * fuel.getDensityFuel() * vehicleSpeed) / (3600 * massAirFlow);
    }

    /**
     *
     * @param values
     * @return
     */
    public static double getAverage(List<Double> values) {
        double total = 0.0;

        for (double n : values) {
            total += n;
        }

        return total / values.size();
    }

    /**
     * @param rpm
     * @param map
     * @param iat
     * @return
     */
    public static double getIMAP(int rpm, double map, double iat) {
        return ((rpm * map) / iat) / 2;
    }

    /**
     * @param imap
     * @param vdm
     * @return
     */
    public static double getMAF(double imap, double vdm) {

        // average molecular mass of air
        double mm = 28.97;

        // volumetric efficiency in percent
        int ev = 85;

        // constant kelvin joules per mole
        double r = 8.314;


        return ((imap / 60) * ev * vdm * mm) / r;
    }

    /**
     * @param totalCapacity
     * @param fuelInputPercent
     * @return
     */
    public double getFuelCapacity(double totalCapacity, double fuelInputPercent) {

        // measured in liters
        return totalCapacity * (fuelInputPercent / 100);
    }

    /**
     *
     * @param consumption
     * @param totalCapacity
     * @param fuelInputPercent
     * @return
     */
    public double getFuelCapacityRange(double consumption, double totalCapacity, double fuelInputPercent) {

        // autonomy in km range
        return getFuelCapacity(totalCapacity, fuelInputPercent) * consumption;
    }

    /**
     *
     */
    public enum Fuel {

        Diesel(14.5, 800),
        Ethanol(9, 789),
        Gasoline(14.68, 803),
        GasNatural(17.2, 712),
        E27((0.73 * 14.68) + (0.27 * 9), (0.73 * 803) + (0.27 * 789));

        // measured in g/m3
        double airFuel;

        // measured in g/m3
        double densityFuel;

        Fuel(double airFuel, double densityFuel) {
            this.airFuel = airFuel;
            this.densityFuel = densityFuel;
        }

        public double getAirFuel() {
            return airFuel;
        }

        public double getDensityFuel() {
            return densityFuel;
        }
    }
}