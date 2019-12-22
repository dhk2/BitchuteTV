package anticlimacticteleservices.bitchutetv;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class ChannelRepository {
    private ChannelDao channelDao;
   private LiveData <List<Channel>> allChannels;
  //  private LiveData <Channel> channel;
    private List <Channel> deadChannels;
    public  ChannelRepository(Application application){
        ChannelDatabase database = ChannelDatabase.getChannelDatabase(application);
        channelDao =  database.ChannelDao();
        allChannels = channelDao.getChannels();
        deadChannels =  channelDao.getDeadChannels();
    }
    public void insert(Channel channel){new InsertChannelAsyncTask(channelDao).execute(channel);}
    public void update(Channel channel){new UpdateChannelAsyncTask(channelDao).execute(channel);}
    public void delete(Channel channel){new DeleteChannelAsyncTask(channelDao).execute(channel);}

    public LiveData<List<Channel>> getAllChannels(){return allChannels; }
    public List<Channel> getDeadChannels(){return deadChannels;}

    private static class InsertChannelAsyncTask extends AsyncTask<Channel,Void,Void>{
        private ChannelDao channelDao;

        private InsertChannelAsyncTask(ChannelDao channelDao){
            this.channelDao = channelDao;
        }

        @Override
        protected Void doInBackground(Channel... channels){
            channelDao.insert(channels[0]);
            System.out.println("VR inserting channel "+channels[0].toDebugString());
            return null;
        }
    }

    private static class DeleteChannelAsyncTask extends AsyncTask<Channel,Void,Void>{
        private ChannelDao channelDao;

        private DeleteChannelAsyncTask(ChannelDao channelDao){
            this.channelDao = channelDao;
        }

        @Override
        protected Void doInBackground(Channel... channels){
            channelDao.delete(channels[0]);
            return null;
        }
    }

    private static class UpdateChannelAsyncTask extends AsyncTask<Channel,Void,Void>{
        private ChannelDao channelDao;

        private UpdateChannelAsyncTask(ChannelDao channelDao){
            this.channelDao = channelDao;
        }

        @Override
        protected Void doInBackground(Channel... channels){
            channelDao.update(channels[0]);
            System.out.println("VR updating channel "+channels[0].toDebugString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    public boolean exists(String sourceID){
        System.out.println("starting to check existence");
        if (channelDao.getChannelsBySourceID(sourceID).size() >0){
            System.out.println("false");
            return false;
        }
        System.out.println("true");
        return true;
    }

}

