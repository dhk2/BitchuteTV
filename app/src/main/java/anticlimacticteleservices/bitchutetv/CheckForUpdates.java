package anticlimacticteleservices.bitchutetv;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

class CheckForUpdates extends AsyncTask<String, String, Boolean> {
    private final SimpleDateFormat bdf = new SimpleDateFormat("EEE', 'dd MMM yyyy HH:mm:SS' 'ZZZZ");
    private final SimpleDateFormat ydf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private Document doc;
    private int dupecount=0;
    private int mirror=0;
    private int newcount=0;
    private ArrayList<WebVideo> allWebVideos;
    private ArrayList<Channel> allChannels;
    private static Context context;
    private static Long feedAge;
    private static ChannelDao channelDao;
    private static VideoDao videoDao;
    VideoDatabase videoDatabase;
    ChannelDatabase channelDatabase;
    String updateError="";
    boolean headless=true;
    boolean backgroundSync;
    boolean wifiOnly;
    boolean wifiConnected;
    boolean mobileConnected;
    boolean forceRefresh;
    boolean muteErrors;
    boolean hideWatched;
    int bitchutePlayerChoice;
    long scrapeInterval;
    long updateInterval;
    long channelUpdateInterval;
    int tooEarly=0;
    int tooOld=0;

    public static SharedPreferences preferences;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (!updateError.isEmpty() && !muteErrors){
            Toast.makeText(context, newcount + updateError, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected Boolean doInBackground(String... params) {
        Log.v("Channel-Update","starting on channel update");
        WebVideo nv = null;
        context = SicSync.context;
        headless=true;
        if (headless){
            preferences = context.getSharedPreferences( "anticlimacticteleservices.sic" + "_preferences", MODE_PRIVATE);
            feedAge = preferences.getLong("feedAge",7);
            backgroundSync = preferences.getBoolean("backgroundSync",true);
            wifiOnly = preferences.getBoolean("wifiOnly",false);
            bitchutePlayerChoice = preferences.getInt("bitchutePlayerChoice", 8);
            muteErrors = preferences.getBoolean("muteErrors",true);
            hideWatched = preferences.getBoolean("hideWatched",false);
            updateInterval = preferences.getLong("backgroundUpdateInterval",60);
            channelUpdateInterval = preferences.getLong("channelUpdateInterval", 60);
            scrapeInterval = preferences.getLong("scrapeInterval",60);
        }
        if (null == videoDao){
            channelDatabase = Room.databaseBuilder(context , ChannelDatabase.class, "channel")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
            channelDao = channelDatabase.ChannelDao();
            videoDatabase = Room.databaseBuilder(context, VideoDatabase.class, "mydb")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
            videoDao = videoDatabase.videoDao();
        }
        if (null != videoDao){
        //    allWebVideos =(ArrayList)videoDao.getVideos();
            Log.v("Channel-Update","loaded videos from database"+ allWebVideos.size());
        }
        else {
            Log.e("Channel-Update","unable to access video database");
            return false;
        }
        if (null != channelDao){

           // allChannels = (ArrayList<Channel>) channelDao.getChannels();
            Log.v("Channel-Update","Loaded channel database with "+allChannels.size());
        }
        else{
            Log.e("Channel-Update", "failed to load channel list from sql");
            return false;
        }

channelloop:for (Channel chan :allChannels){
            Long diff = new Date().getTime()- chan.getLastsync();
            //TODO implement variable refresh rate by channel here
            if ((diff>channelUpdateInterval*60*1000) || forceRefresh){
                Log.v("Channel-Update", "Checking "+chan.getAuthor()+" for new videos since "+String.valueOf(diff/1000)+" seconds ago");
                chan.setLastsync(new Date());
                channelDao.update(chan);
                try {
                    doc = Jsoup.connect(chan.getBitchuteRssFeedUrl()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Channel-Update","network failure tying to get bitchute rss feeds "+e.getMessage());
                    chan.incrementErrors();
                    updateError = e.toString();
                    //crash out if unable to reach bitchute
                    if (e.getMessage().indexOf("Unable to resolve host")>0) {
                        return false;
                    }
                }
                if (null==doc){
                    updateError = "failure loading bitchute rss feed ";
                    Log.e("Channel-Update", "null document load for bitchute RSS feed for "+chan.toCompactString());
                    continue channelloop;
                }
                Elements videos = doc.getElementsByTag("item");
   bitchuteLoop:for (Element video : videos) {
                    nv = new WebVideo(video.getElementsByTag("link").first().text());
                    List matches = videoDao.getVideosBySourceID(nv.getSourceID());
                    if (matches.isEmpty()) {
                        Date pd;
                        try {
                            pd = bdf.parse(video.getElementsByTag("pubDate").first().text());
                        } catch (ParseException e) {
                            Log.e("Channel-Update", "date parsing error "+e.getLocalizedMessage()+video);
                            continue bitchuteLoop;
                        }
                        if (pd.getTime() + (feedAge * 24 * 60 * 60 * 1000) < new Date().getTime()) {
                            tooOld++;
                            break bitchuteLoop;
                        }
                        nv.setDate(pd);
                        nv.setAuthorID(chan.getID());
                        nv.setTitle(video.getElementsByTag("title").first().text());
                        nv.setDescription(video.getElementsByTag("description").first().text());
                        nv.setUrl(video.getElementsByTag("link").first().text());
                        nv.setThumbnail(video.getElementsByTag("enclosure").first().attr("url"));
                        nv.setAuthor(chan.getTitle());
                        allWebVideos.add(nv);
                        videoDao.insert(nv);
                        newcount++;
                        if (chan.isNotify()) {
                            createNotification(nv);
                        }
                    }
                    else {
                        dupecount++;
                    }
                }
            }
            else{
                tooEarly++;
            }
        }
        for (WebVideo v : allWebVideos) {
            if (v.getLastScrape() + (scrapeInterval * 60 * 1000) < new Date().getTime()) {
                new VideoScrape().execute(v);
            }
        }

        Log.v("Channel-Update",dupecount+" duplicate videos discarded,"+mirror+" videos mirrored," +newcount+" new videos added, "+tooOld+" videos too old, "+tooEarly+ " channels not checked ");
        Util.scheduleJob(context);
        return true;
    }
    private void createNotification(WebVideo vid){
        String path="";
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int switcher=0;
        Uri uri = Uri.parse(vid.getMp4());
        notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("videoID",vid.getID());
        Bundle bundle = new Bundle();
        bundle.putLong("videoID", vid.getID());
        notificationIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notificationBuilder =
                new NotificationCompat.Builder(context, "anticlimacticteleservices.sic")
                      //TODO put bitchutetv logo here
                        //  .setSmallIcon(R.drawable.sic_round)
                        .setContentTitle(vid.getAuthor())
                        .setContentText(vid.getTitle())
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build();

        NotificationManager notificationManager = context.getSystemService(
                NotificationManager.class);
        notificationManager.notify(((int) vid.getID()), notificationBuilder);
    }
}
