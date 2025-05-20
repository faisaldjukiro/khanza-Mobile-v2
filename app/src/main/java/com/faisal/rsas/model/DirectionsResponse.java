package com.faisal.rsas.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionsResponse {
    @SerializedName("routes")
    public List<Route> routes;

    public static class Route {
        @SerializedName("overview_polyline")
        public OverviewPolyline overviewPolyline;

        @SerializedName("legs")
        public List<Leg> legs;

        public static class OverviewPolyline {
            @SerializedName("points")
            public String points;
        }

        public static class Leg {
            @SerializedName("distance")
            public Distance distance;

            @SerializedName("duration")
            public Duration duration;

            public static class Distance {
                @SerializedName("text")
                public String text;
                public float value;
            }

            public static class Duration {
                @SerializedName("text")
                public String text;
                public int value;
            }
        }
    }
}