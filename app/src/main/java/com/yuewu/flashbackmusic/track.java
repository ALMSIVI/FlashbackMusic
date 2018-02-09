package com.yuewu.flashbackmusic;

import java.util.Calendar;


public class track {

    Calendar cal;
    String trackName;
    int score;
    int status;

    //Constructor
    track(String name)
    {
        Calendar cal;
        trackName = name;
        score = 0;
        status = 0;
    }

    //Update the status (like dislike etc)
    public void updateStatus()
    {
        if(this.status == -1)
        {
            this.status = 0;
        }
        else if(this.status == 0)
        {
            this.status = 1;
        }

        else if(this.status == 1)
        {
            this.status = -1;
        }
    }

    //Update the last played up the song
    public void justPlayed()
    {
        this.cal = Calendar.getInstance();
    }

    //Get the date and time this song was last played
    public String getDateTime()
    {
        int year = this.cal.get(Calendar.YEAR);
        int month = this.cal.get(Calendar.MONTH);      // 0 to 11
        int day = this.cal.get(Calendar.DAY_OF_MONTH);
        int hour = this.cal.get(Calendar.HOUR_OF_DAY);
        int minute = this.cal.get(Calendar.MINUTE);

        String toRet = ("Last Played on: " + month + "/" + day + "/" + year + "at" +
                            hour + ":" + minute);

        return toRet;
    }


}



