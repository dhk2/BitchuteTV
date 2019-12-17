package anticlimacticteleservices.bitchutetv;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class BitchuteHomePage extends AsyncTask<String, String, String> {
    private String resp;
    Document doc;
    String error = "";
    final SimpleDateFormat bvsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    VideoRepository repository;

    @Override
    protected String doInBackground(String... params) {
        String thumbnail = "";
        try {

            doc = Jsoup.connect("https://www.bitchute.com/#listing-subscribed").get();
            System.out.println("loading bitchute home page");
            Elements results = doc.getElementsByClass("video-card");
            loop:
            for (Element r : results) {
                Video nv = new Video("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
                for (Video v : repository.getDeadVideos()) {
                    if (v.getSourceID().equals(nv.getSourceID())) {
                        continue loop;
                    }
                }
                Date pd = new Date();
                nv.setHackDateString(r.getElementsByClass("video-card-published").first().text());
                nv.setTitle(r.getElementsByClass("video-card-title").first().text());
                // nv.setAuthorID(-1l);
                nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
                nv.setViewCount(r.getElementsByClass("video-views").first().text());
                //TODO calculate duration time into milliseconds  r.getElementsByClass("video-duration").first().text()
                nv.setCategory("popular");
                repository.insert(nv);
            }
        } catch (MalformedURLException e) {
            Log.e("Main-Bitchute-Home", "Malformed URL: " + e.getMessage());
            error = e.getMessage();
        } catch (IOException e) {
            Log.e("Main-Bitchute-Home", "I/O Error: " + e.getMessage());
            error = e.getMessage();
        } catch (NullPointerException e) {
            Log.e("Main-Bitchute-Home", "Null pointer exception" + e.getMessage());
            error = e.getMessage();
        }
        if (!error.isEmpty()) {
            return null;
        }
        Elements results = doc.getElementsByClass("video-trending-container");
        //System.out.println(doc);
        for (Element r : results) {
            //  System.out.println("\n\n"+ r);
            Video nv = new Video("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
            //nv.setAuthorID(-1l);
            nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
            nv.setViewCount(r.getElementsByClass("video-views").first().text());
            nv.setTitle(r.getElementsByClass("video-trending-title").first().text());
            nv.setAuthor(r.getElementsByClass("video-trending-channel").first().text());
            nv.setDescription(r.getElementsByClass("video-trending-channel").first().text());
            nv.setHackDateString(r.getElementsByClass("video-trending-details").first().text());
            //System.out.println(r.getElementsByClass("video-duration").first().text());
            nv.setCategory("trending");
            MainActivity.data.addVideo(nv);
        }
        List<Channel> test = Bitchute.getChannels(doc);
        System.out.println(test.size() + " channels found on home page");
        MainActivity.data.addChannels(test);
        System.out.println("parsed " + MainActivity.data.getTrending().size() + " Trending videos");

        results = doc.getElementsByClass("video-card");

        System.out.println("parsed " + MainActivity.data.getAllVideos().size() + " All videos");

        return "done";

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        System.out.println(error);
    }
}