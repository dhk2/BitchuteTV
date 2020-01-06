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
    static ArrayList <Category> categories;
    public static class GetWebVideos extends AsyncTask<String, String, String> {
        String target="";
        String type="";
        Document doc;
        ArrayList <WebVideo> foundVideos;
        @Override
        protected String doInBackground(String... strings) {
            VideoRepository vr = new VideoRepository(MainActivity.data.getApplication());
            ChannelRepository cr = new ChannelRepository(MainActivity.data.getApplication());
            ArrayList <WebVideo> allVideos = vr.getDeadWebVideos();
            String rl = strings[0];
            Log.d("bitchute-getvideos","starting to process video search for "+rl);
            if ((" "+rl).indexOf("/")>0) {
                type = rl.split("/")[1];
                target = rl.split("/")[2];
                System.out.println("("+type+")" + "   <=>   " +"["+ target+"]");
            }
            else {
                type = "channel";
                target = rl;
            }
            if (type.equals("category")){
                for (Category c: categories){
                    if (rl.equals(c.getUrl())){
                        target=c.getName();
                    }
                }
            }
            try {
                doc = Jsoup.connect("https://www.bitchute.com"+rl).get();

                foundVideos= (ArrayList) getVideos(doc);
                System.out.println("found "+foundVideos.size()+" Videos looking for "+type+" "+target);
                if (type.equals("category")){
                    for (WebVideo v : foundVideos){
                        v.setCategory(target);
                    }
                }

                if (type.equals("hashtag")){
                    System.out.println("setting hashtags on videos for hashtag #"+target);
                    for (WebVideo v : foundVideos){
                        if (!v.getHashtags().contains(target)){
                            v.setHashtags("#"+target);
                        };
                    }
                }
        found:  for (WebVideo v :foundVideos){
                    for (WebVideo x : allVideos){
                        if (x.getSourceID().equals(v.getSourceID())){

                            x.smartUpdate(v);
                            if (type.equals("category")){
                                x.setCategory(target);
                                System.out.println("set category "+x.toCompactString());
                            }
                            if (type.equals("hashtag")) {
                                x.setHashtags("#" + target);
                                System.out.println("set hashtag "+x.toCompactString());
                            }
                            Log.d("Bitchute-GetWebVideo","video already in database, updating "+x.toCompactString());
                            vr.update(x);
                            continue found;
                        }
                    }
                    Log.d("Bitchute-GetWebVideo","video not in database, inserting "+v.toCompactString());
                    if (type.equals("category")){
                        v.setCategory(target);
                    }
                    if (type.equals("hashtag")) {
                        v.setHashtags("#" + target);
                    }
                    vr.insert(v);
                    System.out.println("inserted "+v.toCompactString());
                }
            } catch (MalformedURLException e) {
                Log.e("get video string", "Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                Log.e("get video string", "I/O Error: " + e.getMessage());
            } catch (NullPointerException e) {
                Log.e("get video string", "Null pointer exception3 " + e.getMessage());
            }

            return null;
        }
    }
    public static ArrayList <WebVideo> getVideos(Document doc) {
        ArrayList<WebVideo> foundWebVideos = new ArrayList<>();
        WebVideo nv = null;
        try {
            Elements results = doc.getElementsByClass("video-card");
            for (Element r : results) {
                nv = new WebVideo("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
                Date pd = new Date();
                nv.setHackDateString(r.getElementsByClass("video-card-published").first().text());
                Elements author  = r.getElementsByClass("video-card-channel");
                //System.out.println(author.size()+" athour element:"+author);
                // Some video cards appear to lack author information
                if ((author != null) && (author.first() != null)){
                    nv.setAuthor(author.first().text());
                    String ugly = r.getElementsByClass("video-card-channel").first().toString();
                    ugly = ugly.substring(ugly.indexOf("/")+1);
                    ugly = ugly.substring(ugly.indexOf("/")+1);
                    ugly = ugly.substring(0,ugly.indexOf("/"));
                    nv.setAuthorSourceID(ugly);

                }
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
            //System.out.println(doc);
            System.out.println(nv.toDebugString());
            Log.e("Bitchute-get-videos", "Null pointer exception 1" +Log.getStackTraceString(e));

            //System.out.println(doc);
        }
        if (null == doc) {
            System.out.println("failed to load doc");
        } else {
            Elements results = doc.getElementsByClass("video-trending-container");
            for (Element r : results) {
                //  System.out.println("\n\n"+ r);
                   nv = new WebVideo("https://www.bitchute.com" + r.getElementsByTag("a").first().attr("href"));
                //nv.setAuthorID(-1l);
                nv.setThumbnailurl(r.getElementsByTag("img").first().attr("data-src").toString());
                nv.setViewCount(r.getElementsByClass("video-views").first().text());
                nv.setTitle(r.getElementsByClass("video-trending-title").first().text());
                nv.setAuthor(r.getElementsByClass("video-trending-channel").first().text());
                nv.setHackDateString(r.getElementsByClass("video-trending-details").first().text());
                String ugly = r.getElementsByClass("video-trending-channel").first().toString();
                ugly = ugly.substring(ugly.indexOf("/")+1);
                ugly = ugly.substring(ugly.indexOf("/")+1);
                ugly = ugly.substring(0,ugly.indexOf("/"));
                nv.setAuthorSourceID(ugly);
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
            Log.e("get-channels-doc", "Null pointer exception 2" + e.getMessage());
            //System.out.println(doc);
        }
        Log.v("bitchute-class", (Integer.toString(foundChannels.size())) + "Channels found");
        return foundChannels;
    }


    public static List getHashtags(Document doc) {
        List<String> foundHashtags = new ArrayList<String>();
        System.out.println("looking for hastags");
        try {
            Element temp = doc.getElementsByClass("list-inline list-unstyled").first();
            System.out.println(temp);
            Elements results =temp.getElementsByTag("li");
            System.out.println(results.first().text());
            for (Element r : results) {
                System.out.println(r.text());
                foundHashtags.add(r.text());
            }

        } catch (NullPointerException e) {
            Log.e("get-hashtags-doc", "Null pointer exception 2" + e.getMessage());
            //System.out.println(doc);
        }
        Log.v("bitchute-class", (Integer.toString(foundHashtags.size())) + "hashtags found");
        return foundHashtags;
    }

    public static String getCategoryUrl(String name) {
        for (Category c:categories){
            if (c.getName().equals(name)){
                return c.getUrl();
            }
        }
        return "";
    }


    public static ArrayList <Category> initCategories() {
        categories = new ArrayList<Category>();
        categories.add(new Category("Anime & Animation", "/category/animation/", false));
        categories.add(new Category("Arts & Literature", "/category/arts/", false));
        categories.add(new Category("Auto & Vehicles", "/category/vehicles/", false));
        categories.add(new Category("Beauty & Fashion", "/category/beauty/", false));
        categories.add(new Category("Business & Finance", "/category/finance/", false));
        categories.add(new Category("Cuisine", "/category/cuisine/", false));
        categories.add(new Category("DIY & Gardening", "/category/diy/", false));
        categories.add(new Category("Education", "/category/education/", false));
        categories.add(new Category("Entertainment", "/category/entertainment/", false));
        categories.add(new Category("Gaming", "/category/gaming/", false));
        categories.add(new Category("Health & Medical", "/category/health/", false));
        categories.add(new Category("Music", "/category/music/", false));
        categories.add(new Category("News & Politics", "/category/news/", false));
        categories.add(new Category("People & Family", "/category/family/", false));
        categories.add(new Category("Pets & Wildlife", "/category/animals/", false));
        categories.add(new Category("Science & Technology", "/category/science/", false));
        categories.add(new Category("Spirituality & Faith", "/category/spirituality/", false));
        categories.add(new Category("Sports & Fitness", "/category/sport/", false));
        categories.add(new Category("Travel", "/category/travel/", false));
        categories.add(new Category("Vlogging", "/category/vlogging/", false));
        categories.add(new Category("Other", "/category/animation/", false));
        return categories;
    }
    public static ArrayList <Category> getCategories(){
        if (categories ==null || categories.size()==0){
            initCategories();
        }
        return categories;
    }

    public static void setWatched(WebVideo video){
        new BackgroundSetWatched().execute(video);
    }
    public static class BackgroundSetWatched extends AsyncTask<WebVideo, WebVideo, WebVideo> {
        @Override
        protected WebVideo doInBackground(WebVideo... webVideos) {
            VideoRepository vr = new VideoRepository(MainActivity.data.getApplication());
            WebVideo video = webVideos[0];

            if (video.getID()>0){
                video.setWatched(true);
                vr.update(video);
                MainActivity.data.setUpToDate(false);
            }
            else {
                System.out.println("you somehow thought it was a good idea to marked a video watched that doesn't have an id");
            }
            return null;
        }
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
                System.out.println("loaded homepage videos "+foundWebVideos.size());
                List<Channel> foundChannels = getChannels(doc);
                System.out.println("loaded home page channels "+foundChannels.size());
                List<String> foundHashtags = getHashtags(doc);
                System.out.println("found hashtags "+foundHashtags);
                for (WebVideo v: foundWebVideos){
                    System.out.println("Attempting to add video "+v.getAuthorSourceID()+"] "+v.getAuthor()+" ("+v.getSourceID()+") "+v.getTitle());
                    if (!vr.exists(v.getSourceID())){

                        vr.insert(v);
                       // System.out.println("BH VR inserting "+v.toCompactString());
                    }
                    else{
                       // System.out.println( "BH VR not inserting "+v.toCompactString());
                    }

                }
                for (Channel c : foundChannels){
                    //System.out.println("Attempting to add channel "+c.getSourceID()+" "+c.getTitle());
                    c.setYoutubeID("suggested");
                    if (!cr.exists(c.getSourceID())){
                     //   System.out.println("adding new channel "+c.toDebugString());
                        cr.insert(c);
                    }
                    else {
                        //TODO determine if there is a use case where this update would be informative.
                      // System.out.println("Updating scraped channel"+c.toCompactString());
                       for (Channel inception : cr.getDeadChannels()){
                          // System.out.println("["+inception.getSourceID()+"] == ["+c.getSourceID()+"]");
                           if (inception.getSourceID().equals(c.getSourceID())){
                               inception.setYoutubeID("suggested");
                              // System.out.println("this is the channel being updated "+c.toDebugString());
                               cr.update(inception);
                           }
                       }
                    }
                }
                MainActivity.data.trendingHashtags=foundHashtags;
            } catch (MalformedURLException e) {
                Log.e("Bitchute-Home", "Malformed URL: " + e.getMessage());
                error = e.getMessage();
            } catch (IOException e) {
                Log.e("Bitchute-Home", "I/O Error: " + e.getMessage());
                error = e.getMessage();
            } catch (NullPointerException e) {
                Log.e("Bitchute-Home", "Null pointer exception3 " + e.getMessage());
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


