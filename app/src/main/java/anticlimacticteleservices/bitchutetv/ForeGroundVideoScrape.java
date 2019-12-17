package anticlimacticteleservices.bitchutetv;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ForeGroundVideoScrape extends AsyncTask<Video,Video,Video> {
    String error ="";
    VideoRepository repository;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v("Video-Scrape","Pre-execute");
        }
        @Override
        protected void onPostExecute(Video video) {
            super.onPostExecute(video);
            //TODO convert channel calls to livedata source
            Channel test = MainActivity.data.getChannelById(video.getSourceID());
            if (null == test || test.getThumbnail().isEmpty()){
                new ForeGroundChannelScrape();
                System.out.println("launching channel scrape for "+video.getAuthorSourceID());
            }
            Log.v("Video-Scrape","Post-execute"+video.toCompactString());
            //supposedly never supposed to do this, but toast is handy
            if (!error.isEmpty()){
                Toast.makeText(MainActivity.data.getContext(),error, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected Video doInBackground(Video... videos) {

            Video nv = videos[0];
            ArrayList <Video> allVideos = repository.getDeadVideos();

            System.out.println("attempting to scrape \n"+nv.toCompactString());
            try {
                Document doc = Jsoup.connect(nv.getBitchuteUrl()).get();
                //System.out.println(doc);
                if (nv.getCategory().isEmpty()){
                    nv.setCategory(doc.getElementsByClass("video-detail-list").first().getElementsByTag("a").first().text());
                }
                if (nv.getDescription().isEmpty()){
                    nv.setDescription(doc.getElementsByClass("full hidden").toString());
                }
                if (nv.getMagnet().isEmpty()){
                    nv.setMagnet(doc.getElementsByClass("video-actions").first().getElementsByAttribute("href").first().attr("href"));
                }
                if (nv.getMp4().isEmpty()) {
                    nv.setMp4(doc.getElementsByTag("source").attr("src"));
                }
                nv.setHackDateString(doc.getElementsByClass("Video-publish-date").first().text());
                ArrayList<Video> relatedcontent=Bitchute.getVideos(doc);
     related:  for (Video v : relatedcontent) {
                    nv.addRelatedVideos(v.getSourceID());
                    for (Video check : allVideos){
                        if (check.getSourceID().equals(v.getSourceID())){
                            continue related;
                        }
                    }
                    repository.insert(v);
                }
                if (nv.getAuthorID()<1) {
                    Elements channel = doc.getElementsByClass("channel-banner");
                    String c = channel.first().getElementsByAttribute("href").first().attr("href");
                    String g = "";
                    for (String a : c.split("/")) {
                        g = a;
                    }
                    nv.setAuthorSourceID(g);
                    nv.setAuthor((channel.first().getElementsByClass("name").text()));
                }
                System.out.println("preparing to udpate scraped video "+nv.toDebugString());
                repository.update(nv);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Videoscrape","network failure in bitchute scrape for "+nv.toCompactString());
                error = "network failure in bitchute scrape for "+nv.getTitle();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            return nv;
        }
    }
