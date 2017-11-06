package com.heatmap.app.testfragment;

/**
 * Created by Ohya on 2017/11/03.
 */

public class LocalConstatnts {
    public static final int InitialHeatMapMode = R.id.Button_HeatMap_Weighted;

    // 地球の半径(m)
    public static final double Radius = 6356752.314;

    // 南北方向500mに相当する緯度(degree)
    public static final double LatitudeForMeter = 1 / (2 * Math.PI * Radius / 360);

    // 東西方向500mに相当する経度(degree)
    public static double LongitudeForMeter(double latitude)
    {
        return 1 / (2 * Math.PI * Radius *Math.cos(latitude /180 * Math.PI) / 360);
    }
}
