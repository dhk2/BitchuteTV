package anticlimacticteleservices.bitchutetv;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

public class ForeGroundChannelScrape extends AsyncTask<Channel,Channel,Channel> {
    String error ="";
    ChannelRepository cr;
    VideoRepository vr;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.v("Channel-Scrape","Pre-execute");
    }

    @Override
    protected Channel doInBackground(Channel... channels) {
        int updatelevel=0;
        cr = new ChannelRepository(MainActivity.data.getApplication());
        vr = new VideoRepository(MainActivity.data.getApplication());
        Channel oc = null;
        Channel nc = channels[0];
        Channel channelCheck =null;
        for (Channel c: cr.getDeadChannels()){
            if (c.getSourceID().equals(nc.getSourceID())){
                channelCheck=c;
            }
        }
        if (channelCheck != null){
            if (channelCheck.getDescription().isEmpty()) {
                oc = channelCheck;
            }
            else {
                oc = channelCheck;
            }
        }
        try {
            Document doc = Jsoup.connect(nc.getBitchuteUrl()).get();
            nc.setDescription(doc.getElementsByClass("col-md-9 col-sm-8 col-xs-12").text());
            nc.setDateHackString(doc.getElementsByClass("channel-about-details").first().text());
            nc.setTitle(doc.getElementsByClass("name").text());
            nc.setAuthor(nc.getTitle());
            nc.setThumbnail(doc.getElementsByAttribute("data-src").last().attr("data-src"));
            nc.setThumbnailurl(nc.getThumbnail());
            if (nc.getDescription().isEmpty()){
                nc.setDateHackString(nc.getDateHackString());
                if (nc.getDescription().isEmpty()){
                    nc.setDescription("(intentionally left blank)");
                }
            }
            if (oc == null) {
                cr.insert(nc);
            }
            else{
                oc.setDescription(nc.getDescription());
                oc.setDateHackString(nc.getDateHackString());
                oc.setTitle(nc.getTitle());
                oc.setThumbnailurl(nc.getThumbnail());
                oc.setThumbnail(nc.getThumbnailurl());
                oc.setAuthor(nc.getAuthor());
                cr.update(oc);
            }

            ArrayList <WebVideo> relatedWebVideos = Bitchute.getVideos(doc);
  related:  for (WebVideo v : relatedWebVideos){
                for (WebVideo check : vr.getDeadWebVideos()) {
                    if (check.getSourceID().equals(v.getSourceID())) {
                        continue related;
                    }
                }
                v.setAuthorSourceID(nc.getBitchuteID());
                vr.insert(v);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Videoscrape","network failure in bitchute scrape for "+nc.toCompactString());
            error = "network failure in bitchute scrape for "+nc.getTitle();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return nc;
    }
    @Override
    protected void onPostExecute(Channel channel) {
        super.onPostExecute(channel);
        Log.v("Channel-Scrape","Post-execute"+channel.toCompactString());
        //supposedly never supposed to do this, but toast is handy
        if (!error.isEmpty()){
            Toast.makeText(MainActivity.data.getContext(),error, Toast.LENGTH_LONG).show();
        }
    }


}
