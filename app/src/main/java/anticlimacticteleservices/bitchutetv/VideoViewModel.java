package anticlimacticteleservices.bitchutetv;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository repository;
    private LiveData<List<WebVideo>> allVideos;
    private String TAG="VideoViewModel";

    public VideoViewModel(@NonNull Application application) {
        super(application);

        repository = new VideoRepository((application));
        allVideos = repository.getAllVideos();
        Log.i(TAG,"created video view model"+allVideos.getValue());
    }
    public void insert(WebVideo webVideo){
        repository.insert(webVideo);
    }
    public void update(WebVideo webVideo){
        repository.update(webVideo);
    }
    public void delete(WebVideo webVideo){
        repository.delete(webVideo);
    }
    public LiveData<List<WebVideo>> getAllVideos(){
        return  allVideos;
    }
    public  List <WebVideo> getDeadVideos(){return repository.getDeadWebVideos();}

}
