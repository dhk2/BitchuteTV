package anticlimacticteleservices.bitchutetv;

import android.widget.TextView;

import java.util.ArrayList;

public class MasterData {
    ArrayList <Video> feed;
    ArrayList <Video> popular;
    ArrayList <Video> history;
    ArrayList <Video> trending;
    ArrayList <Video> all;
    boolean scraping;
    TextView description;
    ArrayList <Video> scrapedVideos;


    MasterData (){
        feed = new ArrayList<Video>();
        popular = new ArrayList<Video>();
        history = new ArrayList<Video>();
        trending = new ArrayList<Video>();
        all = new ArrayList<Video>();
        scraping=true;
        scrapedVideos=new ArrayList<Video>();
    }
    public ArrayList<Video> getFeed() {
        return feed;
    }

    public void setFeed(ArrayList<Video> feed) {
        this.feed = feed;
    }

    public ArrayList<Video> getPopular() {
        return popular;
    }

    public void setPopular(ArrayList<Video> popular) {
        this.popular = popular;
    }

    public ArrayList<Video> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Video> history) {
        this.history = history;
    }

    public ArrayList<Video> getTrending() {
        return trending;
    }

    public void setTrending(ArrayList<Video> trending) {
        this.trending = trending;
    }

    public ArrayList<Video> getAll() {
        return all;
    }

    public void setAll(ArrayList<Video> all) {
        this.all = all;
    }

    public void addPopular(Video vid){
        popular.add(vid);
        all.add(vid);
    }
    public void updatePopular(Video vid){
        for (Video match:popular){
            if (vid.getSourceID().equals(match.getSourceID())){
                match = vid;
            }
        }
    }
    public void addHistory(Video vid){
        history.add(vid);
    }
    public void addFeed(Video vid){
        feed.add(vid);
        all.add(vid);
    }
    public void addTrending(Video vid){
        trending.add(vid);
        all.add(vid);
    }
    public void addAll(Video vid){
        all.add(vid);
    }

    public boolean isScraping() {
        return scraping;
    }

    public TextView getDescription() {
        return description;
    }

    public void setDescription(TextView description) {
        this.description = description;
    }

    public void setScraping(boolean scraping) {
        this.scraping = scraping;
    }

    public ArrayList<Video> getScrapedVideos() {
        return scrapedVideos;
    }

    public void setScrapedVideos(ArrayList<Video> scrapedVideos) {
        this.scrapedVideos = scrapedVideos;
    }

    public void addScrapedVideos(Video v){
        scrapedVideos.add(v);
    }
    public Video getScrapedVideo(Video v){
        for (Video scraped : scrapedVideos){
            if (v.getSourceID().equals(scraped.getSourceID())){
                System.out.println("scraped video found:"+scraped.toCompactString());
                return scraped;

            }
        }
        return null;
    }
    public Video getVideo(String g){
        if (null==g) {
            System.out.println("trying to find a null video in all"+g);
        }
        else
        {
            for (Video scraped : scrapedVideos) {
                if (!(null==scraped)) {
                    if (g.equals(scraped.getSourceID())) {
                        return scraped;
                    }
                }
            }
            for (Video vid : all) {
                if (g.equals(vid.getSourceID())) {
                    return vid;
                }
            }
        }
        return null;
    }
}
