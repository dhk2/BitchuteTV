package anticlimacticteleservices.bitchutetv;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class VideoRepository {
    private VideoDao videoDao;
    private LiveData <List<WebVideo>> allVideos;
    private LiveData <WebVideo> video;
    private ArrayList <WebVideo> deadWebVideos;
    public VideoRepository(Application application){
        VideoDatabase database = VideoDatabase.getSicDatabase(application);
        videoDao =  database.videoDao();
        allVideos = videoDao.getVideos();
        deadWebVideos = (ArrayList) videoDao.getDeadVideos();
    }
    public void insert (WebVideo webVideo){
        new InsertVideoAsyncTask(videoDao).execute(webVideo);
    }
    public void update(WebVideo webVideo){
        new UpdateVideoAsyncTask(videoDao).execute(webVideo);
    }
    public void delete(WebVideo webVideo){
        new DeleteVideoAsyncTask(videoDao).execute(webVideo);
    }

    public LiveData<List<WebVideo>> getAllVideos(){
        return allVideos;
    }
    public ArrayList<WebVideo> getDeadWebVideos(){return deadWebVideos;}
    public boolean exists(String sourceID){
        if (videoDao.getVideosBySourceID(sourceID).size() >0){
            return true;
        }
        return false;
    }

    private static class InsertVideoAsyncTask extends AsyncTask<WebVideo,Void,Void>{
        private VideoDao videoDao;

        private InsertVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(WebVideo... webVideos){
            WebVideo v =  webVideos[0];
            List<WebVideo> x = videoDao.getVideosBySourceID(v.getSourceID());
            if (x.size()<1) {
                videoDao.insert(v);
            }
            else {
                WebVideo z=x.get(0);
                z.smartUpdate(v);
                videoDao.update(z);
            }
            return null;
        }
    }

    private static class DeleteVideoAsyncTask extends AsyncTask<WebVideo,Void,Void>{
        private VideoDao videoDao;

        private DeleteVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(WebVideo... webVideos){
            videoDao.delete(webVideos[0]);
            return null;
        }
    }

    private static class UpdateVideoAsyncTask extends AsyncTask<WebVideo,Void,Void>{
        private VideoDao videoDao;

        private UpdateVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(WebVideo... webVideos){
            WebVideo v = webVideos[0];
            if (v.getID()>0){
                videoDao.update(v);
            }
            else{
                videoDao.insert(v);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}



