package anticlimacticteleservices.bitchutetv;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MasterData {
    ArrayList <Channel> allChannels;
    ArrayList <WebVideo> allWebVideos;
    Context context;
    Activity activity;
    Application application;
    VideoRepository vr;
    List trendingHashtags;
    List followingHashtags;

    public MasterData (Application application, Context context){
        allWebVideos = new ArrayList<WebVideo>();
        allChannels = new ArrayList<Channel>();
        this.application = application;
        this.context = context;
        vr = new VideoRepository(application);
        allWebVideos = (ArrayList)vr.getDeadWebVideos();
        System.out.println(" size of datg abase"+ allWebVideos.size());
        trendingHashtags=new ArrayList<String>();
        followingHashtags =new ArrayList<String>();
        followingHashtags.add((String) "#maga");

    }
    public Context getContext(){return this.context;}
    public Application getApplication(){return this.application;}

}
