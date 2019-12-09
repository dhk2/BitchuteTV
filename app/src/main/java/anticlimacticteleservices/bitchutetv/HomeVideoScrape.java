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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.DOWNLOAD_SERVICE;

public class HomeVideoScrape extends AsyncTask<Video,Video,Video> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v("Video-Scrape","Pre-execute");
            MainActivity.data.setScraping(true);
        }
        @Override
        protected void onPostExecute(Video video) {
            super.onPostExecute(video);
            Log.v("Video-Scrape","Post-execute");
            MainActivity.data.setScraping(false);
        }
        @Override
        protected Video doInBackground(Video... videos) {
            Video nv = videos[0];
            try {
                Document doc = Jsoup.connect(nv.getBitchuteUrl()).get();
                nv.setCategory(doc.getElementsByClass("video-detail-list").first().getElementsByTag("a").first().text());
                nv.setDescription(doc.getElementsByClass("full hidden").toString());
                nv.setMagnet(doc.getElementsByClass("video-actions").first().getElementsByAttribute("href").first().attr("href"));
                nv.setMp4(doc.getElementsByTag("source").attr("src"));
                MainActivity.data.updatePopular(nv);
                MainActivity.data.setRelated(Bitchute.getVideos(doc));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Videoscrape","network failure in bitchute scrape for "+nv.toCompactString());
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        return null;
        }
    }
