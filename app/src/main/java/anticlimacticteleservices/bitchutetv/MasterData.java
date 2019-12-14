package anticlimacticteleservices.bitchutetv;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MasterData {
    ArrayList <Channel> allChannels;
    ArrayList <Video> allVideos;
    Context context;
    public MasterData (){
        allVideos = new ArrayList<Video>();
        allChannels = new ArrayList<Channel>();
    }
    public MasterData (Context context){
        allVideos = new ArrayList<Video>();
        allChannels = new ArrayList<Channel>();
        this.context = context;
    }
    public Context getContext(){return this.context;}
    public ArrayList<Video> getFeed() {
        return allVideos;
    }

    public ArrayList<Video> getPopular() {
        ArrayList<Video> popular = new ArrayList<Video>();
        for (Video v : allVideos) {
            if (v.getCategory().equals("popular")) {
                popular.add(v);
            }
        }
        return popular;
    }
    public ArrayList<Video> getHistory() {
        ArrayList<Video> history = new ArrayList<Video>();
        for (Video v : allVideos){
            if (v.isWatched()){
                history.add(v);
            };
        }
        return history;
    }
    public ArrayList<Video> getTrending() {
        ArrayList<Video> trending = new ArrayList<Video>();
        for (Video v : allVideos){
            if (v.getCategory().equals("trending")){
                trending.add(v);
            }
        }
        return trending;
    }
    public ArrayList<Video> getAllVideos() {
        return allVideos;
    }
    public void addVideo(Video vid){
        for (Video v : allVideos){
            if (vid.getSourceID().equals(v.getSourceID())){
               // System.out.println("rejecting duplicate video add attempt, database "+v.toCompactString()+"\n attempted add "+vid.toCompactString() );
                if (vid.getMp4().isEmpty()){
                    //System.out.println("attempted add isn't scraaped");
                    return;
                }
                else {
                   // System.out.println("updating database");
                    v=vid;

                    return;
                }
            }
        }
        allVideos.add(vid);
    }
    public void updateVideo(Video vid){
        if (vid.getMp4().isEmpty()){
            System.out.println("trying to update with nonscraped video");
            return;
        }
        for (Video v : allVideos){
            if (v.getSourceID().equals(vid.getSourceID())){
                v=vid;
            }
        }
    }
    public Video getVideo(String g){
        if (null==g) {
            System.out.println("trying to find a null video in allVideos ");
        }
        else
        {
            for (Video v : allVideos) {
                if (!(null==v)) {
                    if (g.equals(v.getSourceID())) {
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
            Video v= (Video) item;
            System.out.println("adding from db "+v.toCompactString());
            addVideo((v));
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
            else{
                System.out.println("This is bullshit "+c.getSourceID()+"+"+Id);

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
    public void addVideos(List <Video> vids){
        for (Video v : vids){
            System.out.println("mass adding video:"+v.toCompactString());
            addVideo(v);
        }
    }
}
