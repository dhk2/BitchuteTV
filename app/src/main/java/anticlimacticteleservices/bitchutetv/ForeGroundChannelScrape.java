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
        Log.v("Video-Scrape","Pre-execute");
    }


    @Override
    protected Channel doInBackground(Channel... channels) {

        Channel nc = channels[0];
        System.out.println("attempting to scrape"+nc.toCompactString());
        try {
            Document doc = Jsoup.connect(nc.getBitchuteUrl()).get();
            System.out.println(doc.getElementsByClass("channel-about-details").text());
            System.out.println(doc.getElementsByClass("col-md-9 col-sm-8 col-xs-12").text());

            ChannelDao channelDao;
            ChannelDatabase channelDatabase;
            channelDatabase = Room.databaseBuilder(MainActivity.data.getContext(), ChannelDatabase.class, "channeldb")
                    .fallbackToDestructiveMigration()
                    .build();
            channelDao = channelDatabase.ChannelDao();
            List test=channelDao.getChannelsBySourceID(nc.getSourceID());
            System.out.println(test.size());
            if (test.size()==0) {
                channelDao.insert(nc);
                System.out.println("added channel to database supposably");
            }
            else{
                channelDao.update(nc);
                System.out.println("updated channel in database");
            }
            System.out.println(channelDao.getChannels().size());
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
