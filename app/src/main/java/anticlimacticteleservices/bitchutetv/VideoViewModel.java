package anticlimacticteleservices.bitchutetv;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository repository;
    private List <Video> allVideos;


    public VideoViewModel(@NonNull Application application) {
        super(application);
        repository = new VideoRepository((application));
        allVideos = repository.getAllVideos();
    }
    public void insert(Video video){
        repository.insert(video);
    }
    public void update(Video video){
        repository.update(video);
    }
    public void delete(Video video){
        repository.delete(video);
    }
    public List <Video> getAllVideos(){
        return (List<Video>) allVideos;
    }
}
