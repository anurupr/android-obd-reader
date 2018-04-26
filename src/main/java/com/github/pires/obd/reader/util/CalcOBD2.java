package com.github.pires.obd.reader.util;


public class CalcOBD2 {

    //private final int SECONDS_HOUR = 3600;

    public CalcOBD2() {

    }

    public static double getFuelConsumption(Fuel fuel, double massAirFlow, int vehicleSpeed) {
        return (fuel.getAirFuel() * fuel.getDensityFuel() * vehicleSpeed) / (3600 * massAirFlow);
        // measured in km/l
    }

    public double getFuelCapacity(double totalCapacity, double fuelInputPercent) {
        return totalCapacity * (fuelInputPercent / 100);
        // measured in liters
    }

    public double getFuelCapacityRange(double consumption, double totalCapacity, double fuelInputPercent) {
        return getFuelCapacity(totalCapacity, fuelInputPercent) * consumption;
        // autonomy in km range
    }

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

    // calc velocidade media
    // calc tempo decorrido
    // calc consumo medio

    // criar uma TRIP com o VIN
    // registrar na memoria os dados velocidade, tempo, consumo quando o motor estiver ligado.
}