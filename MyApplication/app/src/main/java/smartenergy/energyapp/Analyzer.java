package smartenergy.energyapp;

import android.util.Pair;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public final class Analyzer {

    private DBRawHelper dbRawHelper;
    private DBProcessedHelper dbProcessedHelper;
    private DBAggregatedHelper dbAggregatedHelper;

    public Analyzer(DBRawHelper dbRawHelper, DBProcessedHelper dbProcessedHelper, DBAggregatedHelper dbAggregatedHelper) {
        this.dbRawHelper = dbRawHelper;
        this.dbProcessedHelper = dbProcessedHelper;
        this.dbAggregatedHelper = dbAggregatedHelper;
    }


    public double distTraveled(MeanOfTransport vehicle, TimePeriod timePeriod) {
        return distTraveled(vehicle, timePeriod, null);
    }

    //returns the distance (in km)traveled by that particular vehicle over a given timePeriod
    //timePeriod: day, week, month, year, total
    //vehicle: foot, bicycle, car, bus, etc
    public double distTraveled(MeanOfTransport vehicle, TimePeriod timePeriod, Timestamp endDate) {
        return dbAggregatedHelper.getKmsForTransportInPeriod(dbRawHelper, dbProcessedHelper, vehicle, timePeriod, endDate);
    }

    //returns the energy (in ML of gas) consumed by that particular vehicle over given timePeriod
    //params as above
    public double energyUsed(MeanOfTransport vehicle, TimePeriod timePeriod) {
        return Energizer.energyFor(vehicle, distTraveled(vehicle, timePeriod));
    }

    // returns energy in joules consumbed by vehicle over timePeriod
    public double joulesUsed(MeanOfTransport transport, TimePeriod timePeriod) {
        return Energizer.joulesFor(transport, distTraveled(transport, timePeriod));
    }

    //returns the amount of CO2 emitted in grams
    //params as above
    public double co2Emmited(MeanOfTransport vehicle, TimePeriod timePeriod) {
        return Energizer.co2For(vehicle, distTraveled(vehicle, timePeriod));
    }

    // returns the percentage of CO2 one has used compared to a given daily average (this is the thing for the circle progress bar)
    public int percentageCO2comparedToAverg() {
        return (int) Math.round(100*kgCo2Emitted(TimePeriod.DAILY, false) / Energizer.averageKgCo2PerDay);
    }

    public void refreshCaches() {
        dbProcessedHelper.refreshCache(dbRawHelper, dbAggregatedHelper);
    }

    public double kgCo2Emitted(TimePeriod timePeriod) {
        return kgCo2Emitted(timePeriod, true);
    }

    //returns the amount of CO2 saved or wasted in the given timePeriod
    //timePeriod: yearly, monthly
    public double kgCo2Emitted(TimePeriod timePeriod, boolean round) {
        double thisPeriodsUsage = 0;
        for (MeanOfTransport transport : MeanOfTransport.values()) {
            if (transport == MeanOfTransport.STILL) continue;
            thisPeriodsUsage += co2Emmited(transport, timePeriod);
        }
        return round ? round2Decimals(thisPeriodsUsage / 1000) : (thisPeriodsUsage / 1000);
    }

    //timePeriod: yearly, monthly
    public double chfSpent(TimePeriod timePeriod) {
        double thisPeriodsGas = 0;
        for (MeanOfTransport transport : MeanOfTransport.values()) {
            if (transport == MeanOfTransport.STILL) continue;
            thisPeriodsGas += energyUsed(transport, timePeriod);
        }
        return round2Decimals(thisPeriodsGas / 1000 * Energizer.averageGasPrisePerLiter);
    }

    //timePeriod: yearly, monthly
    public double kgsOfWoodUsed(TimePeriod timePeriod) {
        double thisPeriodsJoule = 0;
        for (MeanOfTransport transport : MeanOfTransport.values()) {
            if (transport == MeanOfTransport.STILL) continue;
            thisPeriodsJoule += joulesUsed(transport, timePeriod);
        }
        return round2Decimals(thisPeriodsJoule / Energizer.averageJoulesPerKgWood);
    }

    double round2Decimals(double input) {
        return (double) Math.round(input * 100d) / 100d;
    }

    //this returns an arraylist with Datapoints of the format (date, g CO2)
    //this thing is needed for the graphs
    //timePeriod: monthly (should return the co2 use for every single day); yearly(should return the co2 use for every single month)
    public HashMap<Integer, Double> co2Use(TimePeriod timePeriod) {
        HashMap<Integer, Double> dataPoints = new HashMap<>();

        TimePeriod granularity;
        if (timePeriod.equals(TimePeriod.MONTHLY))
            granularity = TimePeriod.DAILY;
        else if (timePeriod.equals(TimePeriod.YEARLY))
            granularity = TimePeriod.MONTHLY;
        else throw new RuntimeException("Unsupported time period: " + timePeriod);

        for (MeanOfTransport transport : MeanOfTransport.values()) {
            if (transport.equals(MeanOfTransport.STILL)) continue;
            ArrayList<Pair<Integer, Double>> kms = dbAggregatedHelper.getKmsPerPeriod(dbRawHelper, dbProcessedHelper, timePeriod, granularity, transport);
            for (Pair<Integer, Double> pair : kms) { // Iterating over aggregates for given mean of transport
                double co2 = Energizer.co2For(transport, pair.second);
                Double oldCo2 = dataPoints.get(pair.first);
                if (oldCo2 == null) oldCo2 = 0.0;
                dataPoints.put(pair.first, oldCo2 + co2);
            }
        }
        return dataPoints;
    }

    // calculates the distance between two points given by their latitude and longitude
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        if ((lat1 == 0.0 && lng1 == 0.0) || (lat2 == 0.0 && lng2 == 0.0)) {
            return 0.0;
        }
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = (earthRadius * c);
        return dist / 1000; // Convert to Kilometers
    }

}
