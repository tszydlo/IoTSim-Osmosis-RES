package org.cloudbus.res.dataproviders;

import java.time.LocalDateTime;

public interface ForecastData {
    public boolean isForecast();
    public void calculateForecast();
    public double[] getNDayForecast(int n, long timestamp);
    public double[] getNDayForecast(int n, LocalDateTime time);
}
