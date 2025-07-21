package com.creativespacefinder.manhattan.dto;

import com.fasterxml.jackson.annotation.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* ------------------------------------------------------------------
   DTO that maps the 96‑hour /forecast response from OpenWeather
   ------------------------------------------------------------------ */

@JsonIgnoreProperties(ignoreUnknown = true)           // keep lenient at the root
public class ForecastResponse {

    /* ------------- list of 96 hourly records ------------- */
    @JsonProperty("list")
    private List<HourlyForecast> hourly;

    public List<HourlyForecast> getHourly()            { return hourly; }
    public void setHourly(List<HourlyForecast> hourly) { this.hourly = hourly; }

    /* ======================================================
       Inner DTO for each individual hour
       ====================================================== */
    // @JsonIgnoreProperties(ignoreUnknown = true)      // ✱ REMOVED (be strict here)
    public static class HourlyForecast {

        private long dt;                               // epoch‑seconds

        @JsonProperty("main")                          // nested temps
        private TempInfo tempInfo;

        private List<Weather> weather;

        /* ---------- ✱ ADDED: capture any unknown keys ---------- */
        @JsonAnySetter
        void putUnknown(String k, Object v) { unknown.put(k, v); }

        @JsonIgnore
        private final Map<String,Object> unknown = new HashMap<>();
        /* ------------------------------------------------------- */

        /* --------------- getters / helpers ---------------- */
        public long getDt()            { return dt; }
        public void setDt(long dt)     { this.dt = dt; }

        public List<Weather> getWeather()            { return weather; }
        public void setWeather(List<Weather> weather){ this.weather = weather; }

        /** "2025‑07‑15 06:00" in New‑York local time */
        public String getReadableTime() {
            return Instant.ofEpochSecond(dt)
                    .atZone(ZoneId.of("America/New_York"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        public double  getTemp()      { return tempInfo != null ? tempInfo.temp  : 0; }
        public String  getCondition() { return (weather!=null && !weather.isEmpty())
                ? weather.get(0).getMain() : "Unknown"; }

        /* ------- nested "main" object ------- */
        @JsonIgnoreProperties(ignoreUnknown = true)   // keep lenient on the leaf
        public static class TempInfo {
            public double temp;
        }
    }

    /* ======================================================
       Weather entry (first element is used for the summary)
       ====================================================== */
    // @JsonIgnoreProperties(ignoreUnknown = true)     // ✱ REMOVED (be strict)
    public static class Weather {

        private int id;
        private String main;
        private String description;
        private String icon;

        public int getId() {return id;}
        public void setId(int id) {this.id = id; }

        public String getMain()        { return main;        }
        public void   setMain(String m){ this.main = m;      }

        public String getDescription() { return description; }
        public void   setDescription(String d){ this.description = d; }

        public String getIcon()        { return icon; }
        public void   setIcon(String i){ this.icon = i; }
    }
}
