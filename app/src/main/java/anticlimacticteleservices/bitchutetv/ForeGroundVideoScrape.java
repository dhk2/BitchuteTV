package anticlimacticteleservices.bitchutetv;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class ForeGroundVideoScrape extends AsyncTask<WebVideo, WebVideo, WebVideo> {
    String error ="";
    VideoRepository vr;
    boolean debug=false;


        @Override
        protected WebVideo doInBackground(WebVideo... webVideos) {
            vr =new VideoRepository(MainActivity.data.getApplication());
            WebVideo nv = webVideos[0];
            ArrayList <WebVideo> allWebVideos = vr.getDeadWebVideos();
            System.out.println("dead videos "+allWebVideos.size());
            debug=true;
            System.out.println("attempting to scrape video "+nv.getID()+" ["+nv.getAuthorSourceID()+"] "+nv.getAuthor()+" ("+nv.getSourceID()+") "+nv.getTitle()+" "+nv.getBitchuteUrl());
            try {
                Document doc = Jsoup.connect(nv.getBitchuteUrl()).get();
                System.out.println(" Document title "+doc.title());
                //System.out.println(doc);
                 if (nv.getCategory().isEmpty()){
                    nv.setCategory(doc.getElementsByClass("video-detail-list").first().getElementsByTag("a").first().text());
                    System.out.println("updating categoru");
                 }
                if (nv.getDescription().isEmpty()){
                    nv.setDescription(doc.getElementsByClass("full hidden").toString());
                    System.out.println("updating subscription");
                }
                if (nv.getMagnet().isEmpty()){
                    nv.setMagnet(doc.getElementsByClass("video-actions").first().getElementsByAttribute("href").first().attr("href"));
                    System.out.println("magnet link");
                }
                if (nv.getMp4().isEmpty()) {
                    nv.setMp4(doc.getElementsByTag("source").attr("src"));
                    System.out.println("updating mp4");
                }
                nv.setHackDateString(doc.getElementsByClass("Video-publish-date").first().text());
                nv.setLastScrape(new Date().getTime());
                System.out.println("base data scraped");

                //ArrayList<WebVideo> relatedcontent= (ArrayList) Bitchute.getVideos(doc);
                ArrayList<WebVideo> relatedcontent=new ArrayList<WebVideo>();
                System.out.println(relatedcontent.size()+" related videos");
     related:  for (WebVideo v : relatedcontent) {

                    for (WebVideo check : allWebVideos){
                        if (check.getSourceID().equals(v.getSourceID())){
                         if (debug) System.out.println("related video already in datbase"+nv.getID()+" ["+nv.getAuthorSourceID()+"] "+nv.getAuthor()+" +("+nv.getSourceID()+")"+nv.getTitle());
                            continue related;
                        }
                    }
                    nv.addRelatedVideos(v.getSourceID());
                    vr.insert(v);
                    System.out.println("VR inserted video "+nv.getID()+" ["+nv.getAuthorSourceID()+"] "+nv.getAuthor()+" +("+nv.getSourceID()+")"+nv.getTitle());
                }
                System.out.println("related videos sorted out");


                if (nv.getAuthorSourceID().isEmpty()) {
                    Elements channel = doc.getElementsByClass("channel-banner");
                    String c = channel.first().getElementsByAttribute("href").first().attr("href");
                    String g = "";
                    for (String a : c.split("/")) {
                        g = a;
                    }
                    nv.setAuthorSourceID(g);
                    nv.setAuthor((channel.first().getElementsByClass("name").text()));
                }
                System.out.println("channel author determined");


                vr.update(nv);
                System.out.println("vR Updated video "+nv.getID()+" ["+nv.getAuthorSourceID()+"] "+nv.getAuthor()+" +("+nv.getSourceID()+")"+nv.getTitle());
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
        Log.v("WebVideo-Scrape","Pre-execute");
    }
    @Override
    protected void onPostExecute(WebVideo webVideo) {
        super.onPostExecute(webVideo);
        if (!error.isEmpty()){
            Toast.makeText(MainActivity.data.getContext(),error, Toast.LENGTH_LONG).show();
        }
    }
    }
