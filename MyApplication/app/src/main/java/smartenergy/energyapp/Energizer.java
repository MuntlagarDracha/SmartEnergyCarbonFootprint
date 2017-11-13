package smartenergy.energyapp;

/**
 * Created by sandro on 20.10.17.
 * This class translates vehicle type and distance to CO2.
 */

public class Energizer {

    public final static double averageKgCo2PerDay = 5.64;
    public final static double averageGasPrisePerLiter = 1.5;
    public final static double averageJoulesPerKgWood = 18500000;

    public final static double energyDensityGasolineJoulesPerLiter = 34200000;

    //    Return consumed tons of CO2 as a double
    public static double co2For(MeanOfTransport t, double distanceInKm) {
        double gramsOfCo2PerKm = 0;
        switch (t) {
            case CAR:
                gramsOfCo2PerKm = 186.93;
                break;
            case TRAMWAY:
                gramsOfCo2PerKm = 24.9;
                break;
            case TRAIN:
                gramsOfCo2PerKm = 8.05;
                break;
            case BUS:
                gramsOfCo2PerKm = 100.3;
                break;
            case BOAT:
                gramsOfCo2PerKm = 119.2;
                break;
            case AIRPLANE:
                gramsOfCo2PerKm = 229.5;
                break;
            case FOOT:
                break;
            case BICYCLE:
                break;
            default:
                throw new RuntimeException("Unhandled mean of transport: " + t.toString());
        }
        return gramsOfCo2PerKm * distanceInKm;
    }

    // Amount of Joules per person km
    public static double joulesFor(MeanOfTransport t, double distanceInKm) {
        return energyFor(t, distanceInKm) / 1000 * energyDensityGasolineJoulesPerLiter;
    }

    // Amount of ML of gasoline per person km
    public static double energyFor(MeanOfTransport t, double distanceInKm) {
        double energyPerKm = 0;
        switch (t) {
            case CAR:
                energyPerKm = 94.77;
                break;
            case TRAMWAY:
                energyPerKm = 33.5;
                break;
            case TRAIN:
                energyPerKm = 26.85;
                break;
            case BUS:
                energyPerKm = 47.2;
                break;
            case BOAT:
                energyPerKm = 51.4;
                break;
            case AIRPLANE:
                energyPerKm = 97.5;
                break;
            case FOOT:
                break;
            case BICYCLE:
                break;
            default:
                throw new RuntimeException("Unhandled mean of transport: " + t.toString());
        }
        return energyPerKm * distanceInKm;
    }
}
