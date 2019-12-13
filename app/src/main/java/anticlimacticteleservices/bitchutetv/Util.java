package anticlimacticteleservices.bitchutetv;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

//import static androidx.constraintlayout.Constraints.TAG;

public class Util {
    public static String getHowLongAgo(Long pointInTime){
        Long diff = new Date().getTime()- pointInTime;
        int minutes = (int) ((diff / (1000*60)) % 60);
        int hours   = (int) ((diff / (1000*60*60)) % 24);
        int days = (int) ((diff / (1000*60*60*24)));
        String timehack="";
        if (minutes ==1) {
            timehack= "1 minute ago";
        }
        if (minutes>1){
            timehack = minutes + " minutes ago";
        }
        if (minutes==0) {
            timehack = " ago";
        }
        if (hours==1){
            timehack="1 hour,"+timehack;
        }
        if (hours>1){
            timehack= hours +" hours,"+timehack;
        }
        if (days==1){
            timehack="yesterday";
        }
        if (days>1){
            timehack= days +" days ago";
        }if (days>18000){
            timehack = "Time im-memorial";
        }
        return timehack;
    }


    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, SicSync.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setMinimumLatency(60 * 1000); // Wait at least 5m
        builder.setOverrideDeadline(60 * 60 * 1000); // Maximum delay 60m
        builder.setPersisted(true);

        Log.v("Util-Schedule","scheduling sync service job");
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public static String writeHtml(String html) {

        FileWriter fileWriter = null;
        String output = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "webtorrent.html";
        try {
            fileWriter = new FileWriter(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(html);
        printWriter.close();
        return output;
    }
    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public static class DownloadVideo extends AsyncTask<String, String, String>
    {

        File downloadFolder = null;
        File outputFile = null;

        @Override
        protected String doInBackground(String... strings) {

            try {
                System.out.println("attempt to download url:"+strings[0]);
                URL url = new URL(strings[0]);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("Util-Download", "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());
                }
                downloadFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() );

                if (!downloadFolder.exists()) {
                    downloadFolder.mkdir();
                    Log.e("Util-Download", "Directory Created.");
                }
                String fileName = strings[0].substring(strings[0].lastIndexOf("/"));
                outputFile = new File(downloadFolder,fileName);
                Log.e("Util-Download","downloading file:"+fileName);
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e("Util-Download", "File Created");
                }

                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }

                //Close allVideos connection after doing task
                fos.close();
                is.close();

            } catch (Exception e) {

                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
                Log.e("Util-Download", "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }
}