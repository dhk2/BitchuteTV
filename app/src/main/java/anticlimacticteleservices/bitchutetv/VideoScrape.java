package anticlimacticteleservices.bitchutetv;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.room.Room;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
//TODO move dissenter check outside of site specific sections
//TODO transverse comment subthreads
//TODO pull more useful data

public class VideoScrape extends AsyncTask<Video,Video,Video> {
    static VideoDao videoDao;
    static ChannelDao channelDao;
    Video vid;
    VideoDatabase videoDatabase;
    ChannelDatabase channelDatabase;
    Context context;
    Boolean headless=true;
    private static Long feedAge;
    public static SharedPreferences preferences;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.v("Video-Scrape","Pre-execute");
    }
    @Override
    protected void onPostExecute(Video video) {
        super.onPostExecute(video);
        Log.v("Video-Scrape","Post-execute "+video.toCompactString());
    }
    @Override
    protected Video doInBackground(Video... videos) {
        context = this.context;
        System.out.println("vid-scrape elapsed minutes"+(new Date().getTime()-videos[0].getLastScrape())/60000);
        if (((new Date().getTime()-videos[0].getLastScrape())/60000)<5) {
            return null;
        }
        else {
            System.out.println("made it past the scrape time check");
        }
        context = SicSync.context;

        Log.d("Videoscrape","headless:"+headless);
        vid = videos[0];
  //      preferences = context.getSharedPreferences( "anticlimacticteleservices.sic" + "_preferences", MODE_PRIVATE);
  //      feedAge = preferences.getLong("feedAge",7);
        //TODO fix this
        feedAge=7l;
        Log.d("Videoscrape","preferences loaded:"+feedAge);
        if (null == videoDao){
             videoDatabase = Room.databaseBuilder(context, VideoDatabase.class, "mydb")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
            videoDao = videoDatabase.videoDao();
            channelDatabase = Room.databaseBuilder(context , ChannelDatabase.class, "channel")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
            channelDao = channelDatabase.ChannelDao();
            Log.d("Videoscrape","database connections made");
        }
        Document doctest = null;
        Long pd = vid.getDate();

        vid.setLastScrape(new Date().getTime());
        videoDao.update(vid);
        Log.e("Videoscrape","initialized data for scrape:"+vid.toCompactString());
        if ((pd+(feedAge*24*60*60*1000)<new Date().getTime()) && !vid.getKeep()) {
            Log.e("Videoscrape","Removing expired video from feed \n"+vid.toCompactString());
            if (!(null == vid.getLocalPath())){
                File file = new File(vid.getLocalPath());
                file.delete();
            }
            videoDao.delete((vid));
            return null;
        }
        Log.e("Videoscrape", "Starting bitchute processing");
        if (true){
            Document doc = null;
            int commentcounter=0;
            try {
                doc = Jsoup.connect(vid.getBitchuteUrl()).get();
                vid.setCategory(doc.getElementsByClass("video-detail-list").first().getElementsByTag("a").first().text());
                vid.setDescription(doc.getElementsByClass("full hidden").toString());
                vid.setMagnet(doc.getElementsByClass("video-actions").first().getElementsByAttribute("href").first().attr("href"));
                vid.setMp4(doc.getElementsByTag("source").attr("src"));
                if (vid.getAuthorID()>0) {
                    Channel parent = channelDao.getChannelById(vid.getAuthorID());
                    if (null != parent) {
                        if ((parent.isArchive()) && !vid.getMp4().isEmpty() && (null == vid.getLocalPath())) {
                            Log.v("Videoscrape","downloading "+vid.getMp4()+" to "+vid.getLocalPath());
                            Uri target = Uri.parse(vid.getMp4());
                            File fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            vid.setLocalPath(fpath.getAbsolutePath() + "/" + vid.getSourceID() + ".mp4");
                            DownloadManager downloadManager = (DownloadManager) context.getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(target);
                            request.allowScanningByMediaScanner();
                            //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                            //request.setAllowedOverRoaming(false);
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setTitle(vid.getAuthor());
                            request.setDescription(vid.getTitle());
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, vid.getSourceID() + ".mp4");
                            request.setVisibleInDownloadsUi(true);
                        }
                    }
                }
                videoDao.update(vid);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Videoscrape","network failure in bitchute scrape for "+vid.toCompactString());
                //return null;
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
