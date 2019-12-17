package anticlimacticteleservices.bitchutetv;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class VideoRepository {
    private VideoDao videoDao;
    private LiveData <List<Video>> allVideos;
    private LiveData <Video> video;
    private ArrayList <Video> deadVideos;
    public VideoRepository(Application application){
        VideoDatabase database = VideoDatabase.getSicDatabase(application);
        videoDao =  database.videoDao();
        allVideos = videoDao.getVideos();
        deadVideos = (ArrayList) videoDao.getDeadVideos();
    }
    public void insert (Video video){
        new InsertVideoAsyncTask(videoDao).execute(video);
    }
    public void update(Video video){
        new UpdateVideoAsyncTask(videoDao).execute(video);
    }
    public void delete(Video video){
        new DeleteVideoAsyncTask(videoDao).execute(video);
    }

    public LiveData<List<Video>> getAllVideos(){
        return allVideos;
    }
    public ArrayList<Video> getDeadVideos(){return deadVideos;}


    private static class InsertVideoAsyncTask extends AsyncTask<Video,Void,Void>{
        private VideoDao videoDao;

        private InsertVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(Video... videos){
            videoDao.insert(videos[0]);
            System.out.println("VR inserting video "+videos[0].toDebugString());
            return null;
        }
    }

    private static class DeleteVideoAsyncTask extends AsyncTask<Video,Void,Void>{
        private VideoDao videoDao;

        private DeleteVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(Video... videos){
            videoDao.delete(videos[0]);
            return null;
        }
    }

    private static class UpdateVideoAsyncTask extends AsyncTask<Video,Void,Void>{
        private VideoDao videoDao;

        private UpdateVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(Video... videos){
            videoDao.update(videos[0]);
            System.out.println("VR updating video "+videos[0].toDebugString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}



