package org.cloudbus.res.dataproviders.pvgis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.cloudbus.res.dataproviders.EnergyData;
import org.cloudbus.res.dataproviders.ForecastData;
import org.cloudbus.res.model.pvgis.input.Inputs;
import org.cloudbus.res.model.pvgis.output.Hourly;
import org.cloudbus.res.model.pvgis.output.Outputs;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PVGisResponse implements EnergyData, ForecastData {
    private Inputs inputs;
    private Outputs outputs;

    @Override
    public double getCurrentPower(long timestamp) {
        //it is assumed that output data from PVGIS is sorted
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"));
        return outputs.getHourly().get(dateTime.getDayOfYear()*24+dateTime.getHour()).getSystemPower();
    }

    public double getCurrentPower(LocalDateTime time) {
        //it is assumed that output data from PVGIS is sorted
        return outputs.getHourly().get((time.getDayOfYear()-1)*24+time.getHour()).getSystemPower();
    }


    @Override
    public double getAnnualEnergy() {
        return outputs.getHourly()
                .stream()
                .map(Hourly::getSystemPower)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private double base_forecast[];
    private List<Double> hours_forecast[];
    private List<Double> days_forecast;

    private double calculatePowerForDay(int day){
        double sum = 0.0;

        for(int h=0; h<24; h++){
            sum += outputs.getHourly().get(day*24+h).getSystemPower();
        }

        return sum;
    }

    private double calculateBasePower(){
        double result=0;

        for(int i=0; i<24; i++){
            base_forecast[i]=hours_forecast[i].stream().max(Double::compareTo).get();
            result += base_forecast[i];
        }
        return result;
    }

    private void addHoursForecast(int day, int clean){
        for(int h=0; h<24; h++){

            if (day*24+h < outputs.getHourly().size()) {
                hours_forecast[h].add(outputs.getHourly().get(day * 24 + h).getSystemPower());
            }

            while (hours_forecast[h].size()>clean){
                hours_forecast[h].remove(0);
            }
        }
    }

    public void calculateForecast(){
        int BASE=14;
        hours_forecast = new List[24];
        base_forecast = new double[24];
        for(int k=0; k<24; k++){
            hours_forecast[k] = new ArrayList<>();
        }
        days_forecast = new ArrayList<>();

        for(int i=0; i<BASE; i++){
            addHoursForecast(i, BASE);
        }

        for(int day=0; day<outputs.getHourly().size()/24; day++){
            double basePower = calculateBasePower();
            double dayPower = calculatePowerForDay(day);

            double forecast = dayPower/basePower;

            if (forecast>1.0){
                forecast=1.0;
            }

            days_forecast.add(forecast);

            addHoursForecast(day+BASE, BASE);

            System.out.println("DAY="+day+" forecast="+forecast);
        }
    }


    @Override
    public boolean isForecast() {
        return true;
    }

    @Override
    public double[] getNDayForecast(int n, long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.of("UTC"));

        return getNDayForecast(n, dateTime);
    }

    @Override
    public double[] getNDayForecast(int n, LocalDateTime time) {
        double result[] = new double[n];

        int day = time.getDayOfYear();

        for(int i=0; i<n; i++){
            if ((day+i) > days_forecast.size()){
                result[i]=0.5;
            } else {
                result[i] = days_forecast.get(day + i);
            }
        }
        return result;

    }
}
