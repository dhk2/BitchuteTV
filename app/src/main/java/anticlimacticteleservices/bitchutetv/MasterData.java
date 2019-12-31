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
    Boolean upToDate;

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
        upToDate=false;
       // followingHashtags.add((String) "#anime");

    }
    public Context getContext(){return this.context;}
    public Application getApplication(){return this.application;}

    public Boolean isUpToDate() {
        return upToDate;
    }

    public void setUpToDate(Boolean upToDate) {
        this.upToDate = upToDate;
    }
}
