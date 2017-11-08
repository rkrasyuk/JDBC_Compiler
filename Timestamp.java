package com.company;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Timestamp {
    public static String getTimeStamp(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        return df.format(new Date());
    }
}