package anticlimacticteleservices.bitchutetv;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

public class Bitchute {

    public static ArrayList getVideos(Document doc){
        ArrayList<Video> foundVideos = new ArrayList<>();

        try {
            Elements results = doc.getElementsByClass("video-card");
            for (Element r : results){
                Video nv = new Video("https://www.bitchute.com"+r.getElementsByTag("a").first().attr("href"));
                Date pd = new Date();
                nv.setHackDateString(r.getElementsByClass("video-card-published").first().text());
                nv.setTitle(r.getElementsByClass("video-card-title").first().text());
                nv.setAuthorID(-1l);
                nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
                nv.setViewCount(r.getElementsByClass(    "video-views").first().text());
                //TODO calculate duration time into milliseconds  r.getElementsByClass("video-duration").first().text()
                foundVideos.add(nv);
                //System.out.println(nv.toDebugString());
            }

        } catch(NullPointerException e){
            Log.e("Main-Bitchute-Home","Null pointer exception"+e.getMessage());
            //System.out.println(doc);
        }
        if (null==doc) {
            System.out.println("failed to load doc");
        } else {
            Elements results = doc.getElementsByClass("video-trending-container");
            for (Element r : results) {
                //  System.out.println("\n\n"+ r);
                Video nv = new Video("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
                nv.setAuthorID(-1l);
                nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
                nv.setViewCount(r.getElementsByClass("video-views").first().text());
                nv.setTitle(r.getElementsByClass("video-trending-title").first().text());
                nv.setAuthor(r.getElementsByClass("video-trending-channel").first().text());
                nv.setDescription(r.getElementsByClass("video-trending-channel").first().text());
                nv.setHackDateString(r.getElementsByClass("video-trending-details").first().text());
                //System.out.println(r.getElementsByClass("video-duration").first().text());
                foundVideos.add(nv);
            }

        }
        Log.v("bitchute-class", (Integer.toString(foundVideos.size()))+"Videos found");
        return foundVideos;
    }
    public static ArrayList getChannels (Document doc) {
        ArrayList<Channel> foundChannels = new ArrayList<Channel>();
        try {
            Elements results = doc.getElementsByClass("channel-card");
            System.out.println(results.first().text());
            System.out.println(results.text());
            System.out.println(results);
            for (Element r : results){
                Channel nc = new Channel(("https://www.bitchute.com"+r.getElementsByTag("a").first().attr("href")));
                nc.setThumbnailurl(r.getElementsByAttribute("data-src").last().attr("data-src"));
                nc.setThumbnail(nc.getThumbnailurl());
                nc.setAuthor(r.text());
                nc.setTitle(r.text());
                System.out.println(nc.toCompactString());
                foundChannels.add(nc);
            }

        } catch(NullPointerException e){
            Log.e("Main-Bitchute-Home","Null pointer exception"+e.getMessage());
            //System.out.println(doc);
        }

        return foundChannels;
    }
}
