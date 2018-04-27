package com.github.pires.obd.reader.util;

import java.util.List;

public class CalcOBD2 {

    public static double getConsumption(Fuel fuel, double massAirFlow, int vehicleSpeed) {

        if (massAirFlow == 0.0)
            return 0.0;

        if (vehicleSpeed < 1)
            vehicleSpeed = 1;

        // measured in km/l
        return (fuel.getAirFuelRatio() * fuel.getDensity() * vehicleSpeed) / (3600 * massAirFlow);
    }

    public static double getAverage(List<Double> values) {
        double total = 0.0;

        for (double n : values) {
            total += n;
        }

        return total / values.size();

    }

    public double getTankFuelCapacity(double totalCapacity, double fuelInputPercent) {
        return totalCapacity * (fuelInputPercent / 100);
        // measured in liters
    }

    public double getFuelCapacityRange(double consumption, double totalCapacity, double fuelInputPercent) {
        return getTankFuelCapacity(totalCapacity, fuelInputPercent) * consumption;
        // autonomy in km range
    }

    public enum Fuel {

        Diesel(14.5, 800),
        Ethanol(9, 789),
        Gasoline(14.68, 803),
        GasNatural(17.2, 712),
        E27((0.73 * 14.68) + (0.27 * 9), (0.73 * 803) + (0.27 * 789));

        // measured in g
        double airFuelRatio;

        // measured in g
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


    // calc velocidade media
    // calc tempo decorrido
    // calc consumo medio

    // criar uma TRIP com o VIN
    // registrar na memoria os dados velocidade, tempo, consumo quando o motor estiver ligado.
}