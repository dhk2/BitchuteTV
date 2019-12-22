package anticlimacticteleservices.bitchutetv;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ChannelViewModel extends AndroidViewModel {
    private ChannelRepository repository;
    private LiveData <List<Channel>> allChannels;
    private List deadChannels;


    public ChannelViewModel(@NonNull Application application) {
        super(application);
        System.out.println("creating channel view model");

        repository = new ChannelRepository((application));
        allChannels = repository.getAllChannels();
        //System.out.println("created channel view model"+allChannels.getValue());
    }
    public void insert(Channel channel){
        repository.insert(channel);
    }
    public void update(Channel channel){
        repository.update(channel);
    }
    public void delete(Channel channel){
        repository.delete(channel);
    }
    public LiveData<List<Channel>> getAllChannels(){
        return  allChannels;
    }
    public  List <Channel> getDeadChannels(){return repository.getDeadChannels();}

}
