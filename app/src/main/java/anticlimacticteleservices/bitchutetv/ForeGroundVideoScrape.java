package anticlimacticteleservices.bitchutetv;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ForeGroundVideoScrape extends AsyncTask<WebVideo, WebVideo, WebVideo> {
    String error ="";
    VideoRepository vr;
    boolean debug=false;


        @Override
        protected WebVideo doInBackground(WebVideo... webVideos) {
            Log.d("FVS-async","actual video scrape started");
            vr =new VideoRepository(MainActivity.data.getApplication());
            WebVideo video = webVideos[0];
            WebVideo nv = new WebVideo(video.getBitchuteTestUrl());
            Document doc;
            ArrayList <WebVideo> allWebVideos = vr.getDeadWebVideos();
            System.out.println("dead videos "+allWebVideos.size());
            debug=false;
            Log.d("fvs-start",video.toCompactString());
            try {
                doc = Jsoup.connect(nv.getBitchuteUrl()).get();

                nv.setCategory(doc.getElementsByClass("video-detail-list").first().getElementsByTag("a").first().text());
                nv.setDescription(doc.getElementsByClass("full hidden").toString());
                nv.setMagnet(doc.getElementsByClass("video-actions").first().getElementsByAttribute("href").first().attr("href"));
                nv.setMp4(doc.getElementsByTag("source").attr("src"));
                nv.setHackDateString(doc.getElementsByClass("Video-publish-date").first().text());
                String h1 = nv.getHackDateString();
                String h2=h1.substring(19,28);
                String h3 = h1.substring(32);
                h3 = h3.substring(0,h3.length()-1);
                System.out.println(h1+":"+h2+":"+h3);
                //SimpleDateFormat when=new SimpleDateFormat("MMM d Y");
                        //new SimpleDateFormat("M d  , Y HH:MM z");
              //  "January 4th, 2020T00:59 UTC"
                     //   "M dth, YTHH:MM z"
                try {
                   // Date past = when.parse(h3+" "+h2);
                    Date past = new SimpleDateFormat("MMM dd',' yyyyHH:mm zzz")
                            .parse((h3+h2).replaceAll("(?<=\\d)(st|nd|rd|th)", ""));

                   // System.out.println(when.toString()+" ><  "+past);
                    nv.setDate(past);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                nv.setLastScrape(new Date().getTime());
                System.out.println("base data scraped");

                ArrayList<WebVideo> relatedcontent= (ArrayList) Bitchute.getVideos(doc);
               // ArrayList<WebVideo> relatedcontent=new ArrayList<WebVideo>();
                System.out.println(relatedcontent.size()+" related videos");
     related:  for (WebVideo v : relatedcontent) {
                     if ((null == v.getSourceID()) || v.getSourceID().isEmpty() || v.getSourceID().equals("video")) {
                         Log.e("FGVS - related content","problem with related video" +v.toCompactString());
                         continue related;
         }
                    for (WebVideo check : allWebVideos){
                        if (check.getSourceID().equals(v.getSourceID())) {
                            System.out.println("related video already in database" + check.toCompactString());
                            if (check.smartUpdate(v)){
                                Log.d("fvs","updated related video in database");
                                vr.update(check);
                                continue related;
                            }
                            nv.addRelatedVideos(v.getSourceID());
                            continue related;
                        }

                    }
                    nv.addRelatedVideos(v.getSourceID());
                    vr.insert(v);
                    Log.d("VFS-Related","inserted related video "+v.toCompactString());
                }
                Elements channel = doc.getElementsByClass("channel-banner");
                String c = channel.first().getElementsByAttribute("href").first().attr("href");
                String g = "";
                for (String a : c.split("/")) {
                    g = a;
                }
                nv.setAuthorSourceID(g);
                nv.setAuthor((channel.first().getElementsByClass("name").text()));
                System.out.println("channel author determined");

                Elements tags = doc.getElementsByTag("tags");
                for (Element t : tags){
                    System.out.println("hash tags:"+t.text());
                }
                System.out.println(("hashtags sorted"));
                video.smartUpdate(nv);
                vr.update(video);
                Log.d("FVS-done",video.toCompactString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Videoscrape","network failure in bitchute scrape for "+nv.toCompactString());
                error = "network failure in bitchute scrape for "+nv.getTitle();
            } catch (NullPointerException e){
                e.printStackTrace();
                Log.e("foreground video scrape","null pointer esception ");
            }
            return nv;
        }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.v("FVS","Pre-execute");
    }
    @Override
    protected void onPostExecute(WebVideo webVideo) {
        super.onPostExecute(webVideo);
        if (!error.isEmpty()){
            Toast.makeText(MainActivity.data.getContext(),error, Toast.LENGTH_LONG).show();
        }
    }
    }
