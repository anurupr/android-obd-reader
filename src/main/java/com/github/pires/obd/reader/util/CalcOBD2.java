package com.github.pires.obd.reader.util;


/**
 *
 */
public class CalcOBD2 {

    /**
     *
     * @param fuel
     * @param vehicleSpeed
     * @param massAirFlow
     * @return
     */
    public static double getFuelConsumptionMAF(Fuel fuel, int vehicleSpeed, double massAirFlow) {

        if (massAirFlow <= 0)
            return 0;

        if (vehicleSpeed < 1)
            return 0;

        // measured in km/l
        return (fuel.getAirFuelRatio() * fuel.getDensity() * vehicleSpeed) / (3600 * massAirFlow);
    }

    /**
     * @param fuel
     * @param bhp
     * @param vehicleSpeed
     * @param throttlePosition
     * @return
     */
    public static double getFuelConsumptionThrottle(Fuel fuel, int bhp, int vehicleSpeed, double throttlePosition) {

        if (throttlePosition < 0)
            return 0;

        if (vehicleSpeed < 1)
            return 0;

        double massAirFlow = (bhp / 1.25) * (throttlePosition / 100);

        // Eficiencia
        massAirFlow = massAirFlow * 0.50;

        if (massAirFlow <= 0)
            return 0;

        if (vehicleSpeed < 1)
            vehicleSpeed = 1;

        // measured in km/l
        return (fuel.getAirFuelRatio() * fuel.getDensity() * vehicleSpeed) / (3600 * massAirFlow);
    }

    /**
     * @param fuel
     * @param bhp
     * @param vehicleSpeed
     * @param rpm
     * @return
     */
    public static double getFuelConsumptionRPM(Fuel fuel, int bhp, int vehicleSpeed, double rpm) {

        if (rpm < 0)
            return 0;

        if (vehicleSpeed < 1)
            return 0;


        double massAirFlow = (bhp / 1.25) * (rpm / 8000);

        // Eficiencia
        massAirFlow = massAirFlow * 0.50;

        if (massAirFlow <= 0)
            return 0;

        if (vehicleSpeed < 1)
            vehicleSpeed = 1;

        // measured in km/l
        return (fuel.getAirFuelRatio() * fuel.getDensity() * vehicleSpeed) / (3600 * massAirFlow);
    }


    /**
     *
     * @param total
     * @param divisor
     * @return
     */
    public static double getAverage(double total, int divisor) {
        return total / divisor;
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
        double airFuelRatio;

        // measured in g/m3
        double density;

        Fuel(double airFuelRatio, double density) {
            this.airFuelRatio = airFuelRatio;
            this.density = density;
        }

        public double getAirFuelRatio() {
            return airFuelRatio;
        }

        public double getDensity() {
            return density;
        }
    }
}