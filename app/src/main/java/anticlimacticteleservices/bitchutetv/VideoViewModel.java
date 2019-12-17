package anticlimacticteleservices.bitchutetv;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository repository;
    private LiveData<List<Video>> allVideos;


    public VideoViewModel(@NonNull Application application) {
        super(application);
        System.out.println("creating video view model");

        repository = new VideoRepository((application));
        allVideos = repository.getAllVideos();
        System.out.println("created video view model"+allVideos.getValue());
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
    public LiveData<List<Video>> getAllVideos(){
        return  allVideos;
    }

}
