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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bitchute {

    public static ArrayList <WebVideo> getVideos(Document doc) {
        ArrayList<WebVideo> foundWebVideos = new ArrayList<>();

        try {
            Elements results = doc.getElementsByClass("video-card");
            for (Element r : results) {
                WebVideo nv = new WebVideo("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
                Date pd = new Date();
                nv.setHackDateString(r.getElementsByClass("video-card-published").first().text());
                nv.setTitle(r.getElementsByClass("video-card-title").first().text());
                nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
                nv.setViewCount(r.getElementsByClass("video-views").first().text());
                //TODO calculate duration time into milliseconds  r.getElementsByClass("video-duration").first().text()
                if (nv.getCategory().isEmpty()){
                    nv.setCategory("popular");
                }
                foundWebVideos.add(nv);

            }

        } catch (NullPointerException e) {
            Log.e("Main-Bitchute-Home", "Null pointer exception 1" + e.getMessage());
        }
        if (null == doc) {
            System.out.println("failed to load doc");
        } else {
            Elements results = doc.getElementsByClass("video-trending-container");
            for (Element r : results) {
                //  System.out.println("\n\n"+ r);
                WebVideo nv = new WebVideo("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
                //nv.setAuthorID(-1l);
                nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
                nv.setViewCount(r.getElementsByClass("video-views").first().text());
                nv.setTitle(r.getElementsByClass("video-trending-title").first().text());
                nv.setAuthor(r.getElementsByClass("video-trending-channel").first().text());
                nv.setDescription(r.getElementsByClass("video-trending-channel").first().text());
                nv.setHackDateString(r.getElementsByClass("video-trending-details").first().text());
                //System.out.println(r.getElementsByClass("video-duration").first().text());
                if (nv.getCategory().isEmpty()){
                    nv.setCategory("trending");
                }
                foundWebVideos.add(nv);
            }

        }
        Log.v("bitchute-class", (Integer.toString(foundWebVideos.size())) + "Videos found");
        return foundWebVideos;
    }

    public static ArrayList getChannels(Document doc) {
        ArrayList<Channel> foundChannels = new ArrayList<Channel>();
        try {
            Elements results = doc.getElementsByClass("channel-card");
            //System.out.println(results.first().text());
           // System.out.println(results.text());
           // System.out.println(results);
            for (Element r : results) {
                Channel nc = new Channel(("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href")));
                nc.setThumbnailurl(r.getElementsByAttribute("data-src").last().attr("data-src"));
                nc.setThumbnail(nc.getThumbnailurl());
                nc.setAuthor(r.text());
                nc.setTitle(r.text());
                //System.out.println(nc.toCompactString());
                foundChannels.add(nc);
            }

        } catch (NullPointerException e) {
            Log.e("Main-Bitchute-Home", "Null pointer exception 2" + e.getMessage());
            //System.out.println(doc);
        }
        Log.v("bitchute-class", (Integer.toString(foundChannels.size())) + "Channels found");
        return foundChannels;
    }
    public static void getHomePage(){
        new BitchuteHomePage().execute("https://www.bitchute.com");
    }
    public static class BitchuteHomePage extends AsyncTask<String, String, String> {
        Document doc;
        String error = "";
        //final SimpleDateFormat bvsdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        VideoRepository vr = new VideoRepository(MainActivity.data.getApplication());
        ChannelRepository cr = new ChannelRepository(MainActivity.data.getApplication());
        @Override
        protected String doInBackground(String... params) {
            try {
                doc = Jsoup.connect("https://www.bitchute.com").get();
                System.out.println("loaded bitchute home page");
                ArrayList<WebVideo> foundWebVideos = getVideos(doc);
                System.out.println("loaded homepage videos");
                List<Channel> foundChannels = getChannels(doc);
                System.out.println("loaded home page channels");
                for (WebVideo v: foundWebVideos){
                    System.out.println("Attempting to add video "+v.getAuthorSourceID()+"] "+v.getAuthor()+" ("+v.getSourceID()+") "+v.getTitle());
                    if (!vr.exists(v.getSourceID())){

                        vr.insert(v);
                        System.out.println("BH VR inserting "+v.toCompactString());
                    }
                    else{
                        System.out.println( "BH VR not inserting "+v.toCompactString());
                    }

                }
                for (Channel c : foundChannels){
                    System.out.println("Attempting to add channel "+c.getSourceID()+" "+c.getTitle());
                    if (!cr.exists(c.getSourceID())){
                        System.out.println("adding new channel "+c.getSourceID()+" "+c.getTitle());
                        cr.insert(c);
                    }
                    else {
                        //TODO determine if there is a use case where this update would be informative.
                       System.out.println("Updating scraped channel");
                       cr.update(c);
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("Main-Bitchute-Home", "Malformed URL: " + e.getMessage());
                error = e.getMessage();
            } catch (IOException e) {
                Log.e("Main-Bitchute-Home", "I/O Error: " + e.getMessage());
                error = e.getMessage();
            } catch (NullPointerException e) {
                Log.e("Main-Bitchute-Home", "Null pointer exception3 " + e.getMessage());
                error = e.getMessage();
            }
            if (!error.isEmpty()) {
                return error;
            }
        return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!error.isEmpty()) {
                Toast.makeText(MainActivity.data.getContext(), error, Toast.LENGTH_LONG);
            }
        }
    }
}


