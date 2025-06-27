package com.example.app006.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LeaveRec {

    public String startDate;
    public String endDate;

    public int getDurationInDays() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            long diff = end.getTime() - start.getTime();
            return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
