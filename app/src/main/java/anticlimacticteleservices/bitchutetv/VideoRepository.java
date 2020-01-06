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
       // System.out.println("starting to check existence "+sourceID);
        if (videoDao.getVideosBySourceID(sourceID).size() >0){
           // System.out.println("this one says it matches"+videoDao.getVideosBySourceID(sourceID).get(0).toCompactString());
            //System.out.println("false");
            return true;
        }
     //   System.out.println("true");
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
            videoDao.insert(v);
            v = (videoDao.getVideosBySourceID(webVideos[0].getSourceID())).get(0);
          //  System.out.println("VR inserted video "+v.toCompactString());
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
             //   System.out.println("Updating video "+v.getID());
                videoDao.update(v);
            //    System.out.println("VR updated video "+v.getID()+" ["+v.getAuthorSourceID()+"] "+v.getAuthor()+" ("+v.getSourceID()+") "+v.getTitle());
            }
            else{
              //  System.out.println("Attempting to update vidoe without source id, inserting instead");
                videoDao.insert(v);
            //    System.out.println("VR updated video "+v.getID()+" ["+v.getAuthorSourceID()+"] "+v.getAuthor()+" ("+v.getSourceID()+") "+v.getTitle());

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}



