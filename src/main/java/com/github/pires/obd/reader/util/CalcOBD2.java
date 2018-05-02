package com.github.pires.obd.reader.util;


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
        return (fuel.getAirFuel() * fuel.getDensityFuel() * vehicleSpeed) / (3600 * massAirFlow);
        // measured in km/l
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
        return totalCapacity * (fuelInputPercent / 100);
        // measured in liters
    }

    /**
     *
     * @param consumption
     * @param totalCapacity
     * @param fuelInputPercent
     * @return
     */
    public double getFuelCapacityRange(double consumption, double totalCapacity, double fuelInputPercent) {
        return getFuelCapacity(totalCapacity, fuelInputPercent) * consumption;
        // autonomy in km range
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