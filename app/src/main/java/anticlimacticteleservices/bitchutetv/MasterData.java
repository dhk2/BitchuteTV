package anticlimacticteleservices.bitchutetv;

import android.widget.TextView;

import java.util.ArrayList;

public class MasterData {

    ArrayList <Video> all;

    public MasterData (){
        all = new ArrayList<Video>();
    }

    public ArrayList<Video> getFeed() {
        return all;
    }

    public ArrayList<Video> getPopular() {
        ArrayList<Video> popular = new ArrayList<Video>();
        for (Video v : all) {
            if (v.getCategory().equals("popular")) {
                popular.add(v);
            }
        }
        return popular;
    }
    public ArrayList<Video> getHistory() {
        ArrayList<Video> history = new ArrayList<Video>();
        for (Video v : all){
            if (v.isWatched()){
                history.add(v);
            };
        }
        return history;
    }

    public ArrayList<Video> getTrending() {
        ArrayList<Video> trending = new ArrayList<Video>();
        for (Video v : all){
            if (v.getCategory().equals("trending")){
                trending.add(v);
            }
        }
        return trending;
    }

    public ArrayList<Video> getAll() {
        return all;
    }

    public void setAll(ArrayList<Video> all) {
        this.all = all;
    }
    public void addAll(Video vid){
        for (Video v : all){
            if (vid.getSourceID().equals(v.getSourceID())){
                System.out.println("rejecting duplicate video add attempt, database "+v.toCompactString()+"\n attempted add "+vid.toCompactString() );
                if (vid.getMp4().isEmpty()){
                    System.out.println("attempted add isn't scraaped");
                    return;
                }
                else {
                    System.out.println("updating database");
                    v=vid;
                    return;
                }
            }
        }
        all.add(vid);
    }
    public void updateAll(Video vid){
        if (vid.getMp4().isEmpty()){
            System.out.println("trying to update with nonscraped video");
            return;
        }
        for (Video v :  all){
            if (v.getSourceID().equals(vid.getSourceID())){
                v=vid;
            }
        }
    }

    public Video getVideo(String g){
        if (null==g) {
            System.out.println("trying to find a null video in all"+g);
        }
        else
        {
            for (Video v : all) {
                if (!(null==v)) {
                    if (g.equals(v.getSourceID())) {
                        return v;
                    }
                }
            }
        }
        return null;
    }
}
