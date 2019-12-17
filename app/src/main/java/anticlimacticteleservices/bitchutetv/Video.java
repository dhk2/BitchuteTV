package anticlimacticteleservices.bitchutetv;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Entity(tableName = "Feed_Item")
class Video implements Serializable,Comparable<Video>
{
    @PrimaryKey(autoGenerate = true)
    private long ID;
    @ColumnInfo(name = "author_id")
    private long authorID;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "author")
    private String author;
    @ColumnInfo(name = "url")
    private String url;
    @ColumnInfo(name = "watched")
    private boolean watched;
    @ColumnInfo(name = "date")
    private long date;
    @ColumnInfo(name = "currentPosition")
    private long currentPosition;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailurl;
    @ColumnInfo(name = "magnet_link")
    private String magnet;
    @ColumnInfo(name = "video_link")
    private String mp4;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "view_count")
    private String viewCount;
    @ColumnInfo(name = "rating")
    private String rating;
    @ColumnInfo(name = "up_count")
    private String upCount;
    @ColumnInfo(name = "down_count")
    private String downCount;
    @ColumnInfo(name = "source_id")
    private String sourceID;
    @ColumnInfo(name = "comment_count")
    private String commentCount;
    @ColumnInfo(name = "hash_tags")
    private String hashtags;
    @ColumnInfo(name = "category")
    private String category;
    @ColumnInfo(name = "local_Path")
    private String localPath;
    @ColumnInfo(name = "duration")
    private Long duration;
    @ColumnInfo(name = "hackDataString")
    private String hackDateString;
    @ColumnInfo(name = "rank")
    private int rank;
    @ColumnInfo(name = "bitchute_id")
    private String bitchuteID;
    @ColumnInfo(name = "youtube_id")
    private String youtubeID;
    @ColumnInfo(name = "errors")
    private Long errors;
    @ColumnInfo(name = "keep")
    private Boolean keep;
    @ColumnInfo(name = "last_scrape")
    private Long lastScrape;
    @ColumnInfo(name ="related_videos")
    private String relatedVideos;
    @ColumnInfo(name="author_source_id")
    private String authorSourceID;

    public Video()
    {
        this.title = "";
        this.author = "";
        this.url = "";
        this.watched = false;
        this.date = new Date().getTime();
        this.thumbnailurl = "";
        this.magnet = "";
        this.description = "";
        this.mp4 = "";
        this.rating = "";
        this.viewCount = "";
        this.upCount = "";
        this.downCount = "";
        this.sourceID = "";
        this.commentCount = "0";
        this.hashtags = "";
        this.category = "";
        this.currentPosition=0l;
        this.hackDateString="";
        this.youtubeID ="";
        this.bitchuteID ="";
        this.authorID=0;
        this.errors=0l;
        this.keep=false;
        this.lastScrape=0l;
        this.authorSourceID="";
    }

    public Video(String location)
    {
        this.title = "";
        this.author = "";
        this.url = location;
        this.youtubeID ="";
        this.bitchuteID ="";
        if (location.indexOf("youtube") > 0)
        {
            sourceID = location.substring(location.lastIndexOf("?v=") + 3);
            youtubeID =sourceID;
        }
        else
        {
            String[] segments = location.split("/");
            sourceID = segments[segments.length - 1];
            bitchuteID =sourceID;
        }
        this.watched = false;
        this.date = 0;
        this.thumbnailurl = "";
        this.magnet = "";
        this.description = "";
        this.mp4 = "";
        this.rating = "";
        this.viewCount = "";
        this.upCount = "";
        this.downCount = "";
        this.commentCount = "";
        this.hashtags = "";
        this.category = "";
        this.currentPosition=0l;
        this.hackDateString="";
        this.authorID=0;
        this.errors=0l;
        this.keep=false;
        this.lastScrape=0l;
        this.authorSourceID="";

    }

//  	     Getters

    public long getAuthorID() {
        return authorID;
    }
    public String getUpCount() {
        return upCount;
    }

    public void setAuthorID(long authorID) {
        this.authorID = authorID;
    }
    public String getUrl()
    {
        return this.url;

    }
    public long getDate()
    {
        return date;
    }
    public String getDescription()
    {
        return this.description;
    }
    public String getTitle()
    {
        return this.title;
    }
    public String getAuthor()
    {
        return this.author;
    }
    public String getThumbnail()
    {
        return this.thumbnailurl;
    }
    public String getMp4()
    {
        return this.mp4;
    }
    public String getViewCount()
    {
        return this.viewCount;
    }
    public String getRating()
    {
        return this.rating;
    }

    public String getSourceID()
    {
        return this.sourceID;
    }
    public String getEmbeddedUrl(){
        //update for new video sources
        if (url.indexOf("youtube") > 0) {
            return "https://www.youtube.com/embed/"+this.sourceID +"?autoplay=1&modestbranding=1";
        } else {
            return "https://www.bitchute.com/embed/"+this.sourceID;
        }
    }
    public String getYoutubeEmbeddedUrl(){
        return "https://www.youtube.com/embed/"+this.sourceID +"?autoplay=1&modestbranding=1";
    }
    public String getBitchuteEmbeddedUrl(){
        return "https://www.bitchute.com/embed/"+this.sourceID;
    }

//			Setters

    public void setUrl(String value)
    {
        this.url = value;
        if (this.sourceID.isEmpty()) {
            if (value.indexOf("youtube") > 0) {
                sourceID = value.substring(value.lastIndexOf("?v=") + 3);
            } else {
                String[] segments = value.split("/");
                sourceID = segments[segments.length - 1];
            }
        }
    }



    public void setTitle(String value)
    {
        this.title = value;
    }
    public void setAuthor(String value)
    {
        this.author = value;
    }
    public void setThumbnail(String value)
    {
        this.thumbnailurl = value;
    }
    public void setMagnet(String value)
    {
        this.magnet = value;
    }
    public void setDate(long date)
    {
        //System.out.println(date);

        this.date = date;
    }
    public void setDate(Date date){
        this.date=date.getTime();
    }
    public void setMp4(String value)
    {
        this.mp4 = value;
    }
    public void setDescription(String value)
    {
        this.description = value;
    }
    public void setRating(String value)
    {
        this.rating = value;
    }
    public void setUpCount(String value)
    {
        this.upCount = value;
    }
    public void setDownCount(String value)
    {
        this.downCount = value;
    }
    public void setViewCount(String value)
    {
        this.viewCount = value;
    }
    public void setCommentCount(String value)
    {
        this.commentCount = value;
    }
    public void setCategory(String value)
    {
        this.category = value;
    }
    public void setHashtags(String value)
    {
        this.hashtags = value;
    }
    public void setSourceID(String value)
    {
        this.sourceID = value;
    }
//			Functions

    public String toDebugString()  {
        return ("title:" + title + "\n" +
                "url:" + url + "\n" +
                "thumbnail:" + thumbnailurl + "\n" +
                "author:" + author + "\n" +
                "authorID:"+authorID +"\n" +
//			"watched:" + watched.toString() + "\n" +
                "hack date:"+hackDateString+"\n"+
                "description:" + description + "\n" +
                "mp4 file" + mp4 + "\n" +
                "Views:" + viewCount + "\n" +
                "sourceID:" + sourceID + "\n" +
                "Hash tags:" + hashtags  + "\n" +
                "Category:" + category+ "\n");

    }
    public String toHtmlString()  {
        return ("title:" + title + "<p>" +
                "url:" + url + "<p>" +
                "thumbnail:" + thumbnailurl + "<p>" +
                "author:" + author + "<p>" +
                "authorID:"+authorID +"<p>" +
//			"watched:" + watched.toString() + "<p>" +
                "uploaded:" + new Date(date).toString() + "<p>" +
                "magnet link:" + magnet + "<p>" +
                "description:" + description + "<p>" +
                "mp4 file" + mp4 + "<p>" +
                "Rating:" + rating + "<p>" +
                "Views:" + viewCount + "<p>" +
                "Up votes:" + upCount + "<p>" +
                "Down votes:" + downCount + "<p>" +
                "sourceID:" + sourceID + "<p>" +
                "bitchute sourceID:" + bitchuteID + "<p>" +
                "youtube sourceID:" + youtubeID + "<p>" +
                "Comments:" + commentCount + "<p>" +
                "Hash tags:" + hashtags  + "<p>" +
                "local path:"+localPath+"<p>"+
                "Duration:" + duration +"<p>"+
                "Errors:" + errors +"<p>"+
                "keep:" + keep +"<p>"+
                "lastScrape:" + lastScrape +"<p>"+
                "Category:" + category+ "<p>");
    }
    public String toCompactString(){
        String bits ="";
        if (keep)
            bits=bits+" keep";
        if (errors>0)
            bits = bits+ " errors:"+errors;

        return("["+ID+"] ("+authorID+")"+ author +":"+title + " Source ID:"+authorSourceID+ "\n" +
                "Source ID:"+sourceID+" B:"+bitchuteID+" Y:"+youtubeID+" mp4:"+mp4+" local:"+localPath+"url:"+url+"\n"+bits);
    }


    public String toString() {
        return (new Date(date).toString() + " " + title + "  by" + author);
    }
    @Override
    public int compareTo(Video candidate)
    {
        return (this.getDate()>(candidate.getDate())  ? -1 :
                this.getDate()==(candidate.getDate()) ? 0 : 1);
    }

    public boolean isBitchute(){        return (this.bitchuteID.length() > 0);    }
    public String getBitchuteUrl() {return "https://www.bitchute.com/video/"+this.bitchuteID;}
    public String getBitchuteTestUrl() {return "https://www.bitchute.com/video/"+this.sourceID;}
    public boolean isYoutube(){return (this.youtubeID.length()>0); }
    public String getYoutubeUrl(){
        return "https://www.youtube.com/watch?v="+this.youtubeID;
    }

 //   public ArrayList<Comment> getComments(){
//        //comments disabled until they can be roomiied
 //       return null;
 //   }
    public boolean match(String matchID){
        return (matchID.equals(sourceID));
    }

    public long getID() {
        return ID;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getThumbnailurl() {
        return thumbnailurl;
    }

    public void setThumbnailurl(String thumbnailurl) {
        this.thumbnailurl = thumbnailurl;
    }

    public String getMagnet() {
        return magnet;
    }

    public String getDownCount() {
        return downCount;
    }

    public String getHashtags() {
        return hashtags;
    }

    public String getCategory() {
        return category;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isWatched() {
        return watched;
    }

    public String getHackDateString() {
        return hackDateString;
    }

    public void setHackDateString(String hackDateString) {
        this.hackDateString = hackDateString;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getBitchuteID() {
        return bitchuteID;
    }

    public void setBitchuteID(String bitchuteID) {
        this.bitchuteID = bitchuteID;
    }

    public String getYoutubeID() {
        return youtubeID;
    }

    public void setYoutubeID(String youtubeID) {
        this.youtubeID = youtubeID;
    }

    public Long getErrors() {
        return errors;
    }
    public void incrementErrors(){
        errors++;
    }
    public void setErrors(Long errors) {
        this.errors = errors;
    }

    public Boolean getKeep() {
        return keep;
    }

    public void setKeep(Boolean keep) {
        this.keep = keep;
    }

    public Long getLastScrape() {
        return lastScrape;
    }

    public void setLastScrape(Long lastScrape) {
        this.lastScrape = lastScrape;
    }

    public String getRelatedVideos() {
        return relatedVideos;
    }

    public ArrayList <String> getRelatedVideoArray(){
        ArrayList array = new ArrayList<Video>();
        if (null==relatedVideos){
            return array;
        }
        for (String g :relatedVideos.split("\n")){
            array.add(g);
        }
        return array;
    }

    public void setRelatedVideos(String relatedVideos) {
        this.relatedVideos = relatedVideos;
    }

    public void addRelatedVideos(String video) {
        this.relatedVideos = relatedVideos+video+"\n";
    }
    public void setrelatedVideos (ArrayList <String> related){
        String builder ="";
        for (String g : related){
            builder =builder+g+"\n";
        }
        this.relatedVideos=builder;
    }

    public String getAuthorSourceID() {
        return authorSourceID;
    }

    public void setAuthorSourceID(String authorSourceID) {
        this.authorSourceID = authorSourceID;
    }
}