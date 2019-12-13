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
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v("Video-Scrape","Pre-execute");
        }
        @Override
        protected void onPostExecute(Video video) {
            super.onPostExecute(video);
            MainActivity.data.updateVideo(video);
            Log.v("Video-Scrape","Post-execute"+video.toCompactString());
            //supposedly never supposed to do this, but toast is handy
            if (!error.isEmpty()){
                Toast.makeText(MainActivity.data.getContext(),error, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        protected Video doInBackground(Video... videos) {

            Video nv = videos[0];
            System.out.println("attempting to scrape"+nv.toCompactString());
            try {
                Document doc = Jsoup.connect(nv.getBitchuteUrl()).get();
                //System.out.println(doc);
                nv.setCategory(doc.getElementsByClass("video-detail-list").first().getElementsByTag("a").first().text());
                nv.setDescription(doc.getElementsByClass("full hidden").toString());
                nv.setMagnet(doc.getElementsByClass("video-actions").first().getElementsByAttribute("href").first().attr("href"));
                nv.setMp4(doc.getElementsByTag("source").attr("src"));
                nv.setHackDateString(doc.getElementsByClass("Video-publish-date").first().text());
                //MainActivity.data.updateVideo(nv);
                ArrayList<Video> relatedcontent=Bitchute.getVideos(doc);

                for (Video v : relatedcontent) {
                    MainActivity.data.addVideo(v);
                    nv.addRelatedVideos(v.getSourceID());
                }
                Elements channel = doc.getElementsByClass("channel-banner");
                String c = channel.first().getElementsByAttribute("href").first().attr("href");
                String g="";
                for (String a :c.split("/")) {
                    g=a;
                }
                nv.setBitchuteID(g);
                nv.setAuthor((channel.first().getElementsByClass("name").text()));

                VideoDao videoDao;
                VideoDatabase videoDatabase;
                videoDatabase = Room.databaseBuilder(MainActivity.data.getContext(), VideoDatabase.class, "mydb")
                        .fallbackToDestructiveMigration()
                        .build();
                videoDao = videoDatabase.videoDao();
                List test = videoDao.getVideosBySourceID(nv.getSourceID());
                System.out.println(test.size());
                if (test.size()==0) {
                    videoDao.insert(nv);
                    System.out.println("added video to database supposably");
                }
                else{
                    videoDao.update(nv);
                    System.out.println("updated video in database");
                }
                System.out.println(videoDao.getVideos().size());
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
