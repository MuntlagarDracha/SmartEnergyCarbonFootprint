package smartenergy.energyapp;

import java.sql.Timestamp;
import java.util.Calendar;

public enum TimePeriod {
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static Timestamp getTimestampOf(TimePeriod timePeriod){
        return getTimestampOf(timePeriod, null);
    }

    public static Timestamp getTimestampOf(TimePeriod timePeriod, Timestamp timestamp) {
        Calendar c = Calendar.getInstance();
        if(timestamp != null) c.setTimeInMillis(timestamp.getTime());
        switch (timePeriod) {
            case WEEKLY:
                c.set(Calendar.DAY_OF_WEEK, c.getActualMinimum(Calendar.DAY_OF_WEEK));
                c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
                c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
                c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
                c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
                break;
            case YEARLY:
                c.set(Calendar.MONTH, c.getActualMinimum(Calendar.MONTH));
            case MONTHLY:
                c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
            case DAILY:
                c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
            case HOURLY:
                c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
                c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
                c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
                break;
            default:
                return null;
        }
        return new Timestamp(c.getTimeInMillis());
    }

}
