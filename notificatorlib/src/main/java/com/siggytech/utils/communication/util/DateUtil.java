package com.siggytech.utils.communication.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {

    /**
     * Method that gets a name for a picture
     * @return name of picture
     */
    public static String getDateName(){
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.getTimeInMillis());
    }

    public static String getStringDate(long milliseconds){
        String strDate = "";
        try {
            SimpleDateFormat sdf;
            switch (Conf.DATE_FORMAT) {
                case 0:
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    break;
                case 1:
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                    break;
                default:
                    sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
                    break;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(milliseconds);
            strDate = sdf.format(cal.getTime());
        }catch (Exception e){
            e.printStackTrace();
        }
        return strDate;
    }

    public static String getCurrentDate(){
        String strDate = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);

            Calendar cal = Calendar.getInstance();
            strDate = sdf.format(cal.getTime());
        }catch (Exception e){
            e.printStackTrace();
        }
        return strDate;
    }
}
