package anticlimacticteleservices.bitchutetv;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.room.SharedSQLiteStatement;

import java.lang.reflect.Array;
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
    List followingCategories;
    Boolean upToDate;

    public MasterData (Application application, Context context){
        allWebVideos = new ArrayList<WebVideo>();
        allChannels = new ArrayList<Channel>();
        this.application = application;
        this.context = context;
        vr = new VideoRepository(application);
        allWebVideos = (ArrayList)vr.getDeadWebVideos();
        System.out.println(" size of datg abase"+ allWebVideos.size());
        trendingHashtags=loadTrendingHashtags();
        followingHashtags =new ArrayList<String>();
        followingCategories = new ArrayList<String>();
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

    public boolean addTrendingHashtag(String hashTag){
        for (Object g : trendingHashtags){
            if (hashTag.equals(g)){
                return false;
            }
        }
        trendingHashtags.add(hashTag);
        System.out.println(trendingHashtags.size()+" hashtags");
        return true;
    }
    public boolean addFollowingHashtag(String hashTag){
        for (Object g : followingHashtags){
            if (hashTag.equals(g)){
                return false;
            }
        }
        followingHashtags.add(hashTag);
        return true;
    }
    public boolean addFollowingCategory( String category){
        for (Object g : trendingHashtags){
            if (followingCategories.equals(g)){
                return false;
            }
        }
        trendingHashtags.add(category);
        return true;
    }
    public ArrayList<String> loadTrendingHashtags(){
        ArrayList<String> th = new ArrayList<String>();
        th.add("#trump");
        th.add("#covid");
        th.add("#vaccine");
        th.add("#music");
        return th;
    }
}
