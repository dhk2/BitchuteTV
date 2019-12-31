package anticlimacticteleservices.bitchutetv;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "channel")
class Channel implements Serializable{
    @PrimaryKey(autoGenerate = true)
    private long ID;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "author")
    private String author;
    @ColumnInfo(name = "url")
    private String url;
    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailurl;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "profile_image")
    private String profileImage;
    @ColumnInfo(name = "subscribers")
    private String subscribers;
    @ColumnInfo(name = "source_id")
    private String sourceID;
    @ColumnInfo(name = "bitchute_id")
    private String bitchuteID;
    @ColumnInfo(name = "youtube_id")
    private String youtubeID;
    @ColumnInfo(name = "start_date")
    private long joined;
    @ColumnInfo(name = "last_sync")
    private long lastsync;
    @ColumnInfo(name = "last_check")
    private long lastCheck;
    @ColumnInfo(name = "notify")
    private boolean notify;
    @ColumnInfo(name = "archive")
    private boolean archive;
    @ColumnInfo(name ="errors")
    private int errors;
    @ColumnInfo(name="local_file")
    private String localPath;
    @ColumnInfo(name = "supported")
    private boolean supported;
    @ColumnInfo(name="date_hack")
    private String dateHackString;

    @ColumnInfo(name = "subscribed")
    private boolean subscribed;


    public Channel(){
        this.title="";
        this.author="";
        this.url="";
        this.thumbnailurl="";
        this.description="";
        this.profileImage="";
        this.sourceID ="";
        this.youtubeID="";
        this.bitchuteID="";
        this.lastsync=0l;
        this.joined=new Date().getTime();
        this.lastCheck=lastsync;
        this.subscribers="";
        this.notify = false;
        this.archive = false;
        this.errors=0;
        this.localPath="";
        this.supported=false;
        this.dateHackString="";
        this.subscribed=false;
    }
    public Channel(String url) {
        this.url = url;
        description="";
        thumbnailurl="";
        profileImage="";
        author="";
        subscribers="";
        bitchuteID="";
        youtubeID="";
        if (url.indexOf("youtube.com/feeds") > 0)
        {
            this.sourceID = url.substring(url.lastIndexOf("id=") + 3);
        }
        else {
            String[] segments = url.split("/");
            sourceID = segments[segments.length - 1];
        }
        if (url.indexOf("youtube.com")>0){
            youtubeID= sourceID;

        }
        if (url.indexOf("bitchute.com")>0) {
            bitchuteID = sourceID;
        }
        lastsync = 0l;
        joined =new Date().getTime();
        this.lastCheck=lastsync;
        this.notify = false;
        this.archive = false;
        this.errors=0;
        this.localPath="";
        this.supported=false;
        this.dateHackString="";
        this.subscribed=false;
 //       toString();
    }

    public void setUrl(String value){
        if (value.indexOf("youtube.com")>0 && youtubeID.isEmpty()){
            if (value.indexOf("youtube.com/feeds") > 0) {
                youtubeID = value.substring(value.lastIndexOf("id=") + 3);
            }
            else {
                String[] segments = value.split("/");
                youtubeID = segments[segments.length - 1];
            }
            url=(value);
        }
        if (value.indexOf("bitchute.com")>0 && bitchuteID.isEmpty()){
            String[] segments = value.split("/");
            bitchuteID = segments[segments.length - 1];
            url = value;
        }
    }
     public String getBitchuteRssFeedUrl(){
        if (!bitchuteID.isEmpty()){
            return "https://www.bitchute.com/feeds/rss/channel/" + bitchuteID;
        }
        else {
            return "";
        }
    }
    public String getBitchuteUrl() {
        if (!bitchuteID.isEmpty()) {
            return "https://www.bitchute.com/channel/" + bitchuteID;
        } else {
            return "";
        }
    }
    public String getYoutubeRssFeedUrl() {

        if (!youtubeID.isEmpty()){
            return "https://www.youtube.com/feeds/videos.xml?channel_id=" + youtubeID;
        }
        else {
            return "";
        }
    }
    public String getYoutubeUrl() {

        if (!youtubeID.isEmpty()) {
            return "https://www.youtube.com/channel/" + youtubeID;
        } else {
            return "";
        }
    }
    public String toString(){
        return("title:"+this.title+"\n"+
                "author id:"+ID+"\n"+
                "sourceID:"+this.sourceID +"\n"+
                "youtube id:"+youtubeID+"\n"+
                "bitchute id:"+bitchuteID+"\n"+
                "url:"+url+"\n"+
                "thumbnail:"+this.thumbnailurl+"\n"+
                "author:"+this.author+"\n"+
                "profile image"+this.profileImage+"\n"+
                "Subscribers:"+this.subscribers+"\n"+
                "date joined"+new Date(this.joined)+"\n"+
                "Last Sync"+new Date(lastsync)+"\n"+
                "description:"+this.description+"\n"+
                "errors:"+this.errors+"\n"+
                "archive:"+this.archive+"\n"+
                "notify:"+this.notify+"\n"+
                "localpath:"+this.localPath+"\n"+
                "supported:"+this.supported+"\n"+
                "subscribers:"+this.subscribers+"\n");

    }

    public String toCompactString(){
        return("title:"+this.title+"\n"+
                "id:"+ID+" "+this.title+"("+url +")\n"+
                "y:"+youtubeID+"b:"+bitchuteID+" Last Sync:"+new Date(lastsync)+" errors:"+this.errors+"\n");
    }

    public String toDebugString(){
        return("title:"+this.title+"\n"+
                "id:"+ID+" "+this.title+"("+url +")\n"+
                "youtubeid:"+youtubeID+" BitchuteID:"+bitchuteID+" Last Sync:"+new Date(lastsync)+" thumbnail"+this.thumbnailurl+"\n"+
                "Description:"+description);
    }

    public boolean matches(String value){
        return youtubeID.equals(value) || bitchuteID.equals(value);
    }

    public String getSourceID() {
        return this.sourceID;
    }
    public long getJoined() {
        return this.joined;
    }
    public long getLastsync() {
        return this.lastsync;
    }

    public String getSubscribers() {
        return this.subscribers;
    }
    public String getUrl(){
        return this.url;
    }
    public String getDescription(){
        return this.description;
    }
    public String getTitle(){
        return this.title;
    }
    public String getAuthor(){
        return this.author;
    }
    public String getThumbnail(){
        return this.thumbnailurl;
    }
    public void setJoined(Date joined) {
        this.joined = joined.getTime();
    }
    public void setSubscribers(String value){
        this.subscribers=value;
    }
    public void setLastsync(Date lastsync) {
        this.lastsync = lastsync.getTime();
    }
    public void setTitle(String value){
        this.title=value;
    }
    public void setAuthor(String value){
        this.author=value;
    }
    public void setThumbnail(String value){
        this.thumbnailurl=value;
    }
    public void setDescription(String value) {
        this.description = value;
    }
    public void setSourceID(String value){
        this.sourceID = value;
    }
    public String getThumbnailurl() {
        return this.thumbnailurl;
    }
    public void setThumbnailurl(String thumbnailurl) {
        this.thumbnailurl = thumbnailurl;
    }
    public String getProfileImage() {
        return this.profileImage;
    }
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    public String getBitchuteID() {
        return this.bitchuteID;
    }
    public void setBitchuteID(String bitchuteID) {
        this.bitchuteID = bitchuteID;
    }
    public String getYoutubeID() {
        return this.youtubeID;
    }
    public void setYoutubeID(String youtubeID) {
        this.youtubeID = youtubeID;
    }
    public void setJoined(long joined) {
        this.joined = joined;
    }
    public void setLastsync(long lastsync) {
        this.lastsync = lastsync;
    }
    public boolean isBitchute(){
        return !bitchuteID.isEmpty();
    }
    public boolean isYoutube(){
        return !youtubeID.isEmpty();
    }
    public long getID() {
        return this.ID;
    }
    public void setID(long ID) {
        this.ID = ID;
    }
    public boolean isNotify() {
        return notify;
    }
    public void setNotify(boolean notify) {
        this.notify = notify;
    }
    public boolean isArchive() {
        return this.archive;
    }
    public void setArchive(boolean archive) {
        this.archive = archive;
    }
    public int getErrors() {
        return this.errors;
    }
    public void incrementErrors(){
        errors++;
    }
    public void setErrors(int errors) {
        this.errors = errors;
    }
    public String getLocalPath() {
        return localPath;
    }
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    public boolean isSupported() {
        return this.supported;
    }
    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public long getLastCheck() {
        return lastCheck;
    }
    public void setLastCheck(Long bob){lastCheck=bob;}
    public void updateLastCheck() {
        lastCheck =new Date().getTime();
    }

    public String getDateHackString() {
        return dateHackString;
    }

    public void setDateHackString(String dateHackString) {
        this.dateHackString = dateHackString;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
