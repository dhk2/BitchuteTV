package anticlimacticteleservices.bitchutetv;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class VideoRepository {
    private VideoDao videoDao;
    private List <Video> allVideos;

    public VideoRepository(Application application){
        VideoDatabase database = VideoDatabase.getSicDatabase(application);
        videoDao =  database.videoDao();
        allVideos = videoDao.getVideos();
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
    public List <Video> getAllVideos(){
        return allVideos;
    }

    private static class InsertVideoAsyncTask extends AsyncTask<Video,Void,Void>{
        private VideoDao videoDao;

        private InsertVideoAsyncTask(VideoDao videoDao){
            this.videoDao = videoDao;
        }

        @Override
        protected Void doInBackground(Video... videos){
            videoDao.insert(videos[0]);
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
            return null;
        }
    }

}



