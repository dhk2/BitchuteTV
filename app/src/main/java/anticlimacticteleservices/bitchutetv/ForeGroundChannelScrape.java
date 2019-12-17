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

public class ForeGroundChannelScrape extends AsyncTask<Channel,Channel,Channel> {
    String error ="";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.v("Channel-Scrape","Pre-execute");
        System.out.println("passed channel");
    }


    @Override
    protected Channel doInBackground(Channel... channels) {

        Channel nc = channels[0];
        Channel channelCheck = MainActivity.data.getChannelById(nc.getSourceID());
        if (channelCheck != null){
            if (channelCheck.getDescription().isEmpty()) {
                System.out.println("scraping existing but unscraped channel " + channelCheck.toDebugString());
            }
            else {
                System.out.println("rescraping previously scraped chnnel"+channelCheck.toDebugString());
            }
        }
        else{
            System.out.println("scraping brand new channel"+nc.toDebugString());
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
            ChannelDao channelDao;
            ChannelDatabase channelDatabase;
            channelDatabase = Room.databaseBuilder(MainActivity.data.getContext(), ChannelDatabase.class, "channeldb")
                    .fallbackToDestructiveMigration()
                    .build();
            channelDao = channelDatabase.ChannelDao();
            List test=channelDao.getChannelsBySourceID(nc.getSourceID());
            System.out.println(test.size());
            if (test.size()==0) {
                System.out.println("no video in database, adding to database");
                channelDao.insert(nc);
                nc= channelDao.getChannelsBySourceID(nc.getSourceID()).get(0);
            }
            else{
                Channel archivedChannel = (Channel) test.get(0);
                System.out.println("version pulled out of database for comparison" +archivedChannel.toDebugString());
                    archivedChannel.setDescription(nc.getDescription());
                    archivedChannel.setDateHackString(nc.getDateHackString());
                    archivedChannel.setTitle(nc.getTitle());
                    archivedChannel.setThumbnailurl(nc.getThumbnail());
                    archivedChannel.setThumbnail(nc.getThumbnailurl());
                    archivedChannel.setAuthor(nc.getAuthor());
                    channelDao.update(archivedChannel);
                    nc = channelDao.getChannelsBySourceID(nc.getSourceID()).get(0);
            }
            Channel testChannel=MainActivity.data.getChannelById(nc.getSourceID());
            if (testChannel == null){
                System.out.println("nothing in main, adding value" );
                MainActivity.data.addChannel(nc);
                System.out.println(MainActivity.data.getChannelById(nc.getSourceID()));
            }
            else {
                System.out.println("updating main value");
                MainActivity.data.updateChannel(nc);
                System.out.println(MainActivity.data.getChannelById(nc.getSourceID()));
            }
            System.out.println("final db value "+channelDao.getChannelById(nc.getID()));
            ArrayList <Video> videos = Bitchute.getVideos(doc);
            for (Video v : videos){
                v.setAuthorSourceID(nc.getBitchuteID());
                MainActivity.data.addVideo(v);
            }
            //MainActivity.data.addVideos(Bitchute.getVideos(doc));
            System.out.println(channelDao.getChannels().size());
            channelDatabase.close();
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
