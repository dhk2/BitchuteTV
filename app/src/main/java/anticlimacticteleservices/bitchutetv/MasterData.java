package anticlimacticteleservices.bitchutetv;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

public class MasterData {
    ArrayList <Channel> allChannels;
    ArrayList <WebVideo> allWebVideos;
    Context context;
    Activity activity;
    Application application;
    VideoRepository vr;

    public MasterData (Application application, Context context){
        allWebVideos = new ArrayList<WebVideo>();
        allChannels = new ArrayList<Channel>();
        this.application = application;
        this.context = context;
        vr = new VideoRepository(application);
        //allWebVideos = (ArrayList)vr.getAllVideos();
        System.out.println(" size of datg abase"+ allWebVideos.size());
        WebVideo kludge = new WebVideo("https://www.bitchute.com/video/BieayMdvplc/");
        vr.insert(kludge);
    }
    public Context getContext(){return this.context;}
    public Application getApplication(){return this.application;}
  /*
    public ArrayList<WebVideo> getFeed() {
        return allWebVideos;
    }

    public ArrayList<WebVideo> getPopular() {
        ArrayList<WebVideo> popular = new ArrayList<WebVideo>();
        for (WebVideo v : allWebVideos) {
            if (v.getCategory().equals("popular")) {
                popular.add(v);
            }
        }
        return popular;
    }
    public ArrayList<WebVideo> getHistory() {
        ArrayList<WebVideo> history = new ArrayList<WebVideo>();
        for (WebVideo v : allWebVideos){
            if (v.isWatched()){
                history.add(v);
            };
        }
        return history;
    }
    public ArrayList<WebVideo> getTrending() {
        ArrayList<WebVideo> trending = new ArrayList<WebVideo>();
        for (WebVideo v : allWebVideos){
            if (v.getCategory().equals("trending")){
                trending.add(v);
            }
        }
        return trending;
    }
    public ArrayList<WebVideo> getAllVideos() {
        return allWebVideos;
    }
    public void addVideo(WebVideo vid){
        for (WebVideo v : allWebVideos){
            if (vid.getSourceID().equals(v.getSourceID())){
               // System.out.println("rejecting duplicate video add attempt, database "+v.toCompactString()+"\n attempted add "+vid.toCompactString() );
                if (vid.getMp4().isEmpty()){
                    //System.out.println("attempted add isn't scraaped");
                    return;
                }
                else {
                   // System.out.println("updating database");
                    if (v.getID()>vid.getID()){
                        vid.setID(v.getID());
                    }
                    v=vid;
                    vr.update(v);
                    return;
                }
            }
        }
        allWebVideos.add(vid);
        vr.insert(vid);
    }
    public void updateVideo(WebVideo vid){
        if (vid.getMp4().isEmpty()){
            System.out.println("trying to update with nonscraped video");
            return;
        }
        for (WebVideo v : allWebVideos){
            if (v.getSourceID().equals(vid.getSourceID())){
                if (v.getID()>vid.getID()){
                    vid.setID(v.getID());
                }
                v=vid;
                vr.update(v);
            }
        }
    }
    public WebVideo getVideo(String g){
        if (null==g) {
            System.out.println("trying to find a null video in allWebVideos ");
        }
        else
        {
            for (WebVideo v : allWebVideos) {
                if (!(null==v)) {
                    if (g.equals(v.getSourceID())) {
                        System.out.println("returning :"+v.toCompactString());
                        return v;
                    }
                }
            }
        }
        return null;
    }
    public void setAllVideos(ArrayList videos){
        System.out.println("called route "+videos.size());
        for (Object item : videos){
            WebVideo v= (WebVideo) item;
            System.out.println("adding from db "+v.toCompactString());
            addVideo(v);
        }
    }
    public void updateChannel(Channel chan) {
        for (Channel c:allChannels){
            if (c.getSourceID().equals(chan.getSourceID())){
                if (!c.getSourceID().isEmpty()) {
                    System.out.println("trying to overwrite channel "+c.getSourceID() +"with weaker data"+chan.getSourceID());
                    return;
                }
                if (!c.getThumbnail().isEmpty()){
                    System.out.println("tryuing to update thumbnail "+c.getThumbnail()+ " with "+chan.getThumbnail());
                    return;
                }
                c=chan;
            }
        }
    }
    public void deleteChannel(Channel chan) {
        allChannels.remove(chan);
    }
    public Channel getChannelById(String Id){
        for (Channel c: allChannels){
            if (c.getSourceID().equals(Id)){
                System.out.println("found matching channel in db for "+Id);
                return c;
            }
        }
        System.out.println("no database entry for "+Id);
        return null;
    }
    public ArrayList <Channel> getAllChannels() {
        return allChannels;
    }
    public void addChannel(Channel chan){
        for (Channel c : allChannels){
            //System.out.println(chan.toCompactString());
            if (chan.getSourceID().equals(c.getSourceID())) {
                System.out.println("attempting to add an already existing channel");
                return;
            }
        }
        allChannels.add(chan);
    }
    public void addChannels(List<Channel> chans){
        for (Channel c : chans){
            addChannel(c);
        }
    }
    public void addVideos(List <WebVideo> vids){
        for (WebVideo v : vids){
            System.out.println("mass adding video:"+v.toCompactString());
            addVideo(v);
        }
    }
    public void refreshVideos(){
     //   allWebVideos=(ArrayList)vr.getAllVideos();
    }
*/
}
