package anticlimacticteleservices.bitchutetv;

import java.io.Serializable;
import java.util.ArrayList;

public class Category implements Serializable {


    String url;
    String name;
    boolean following;

    public Category(String name) {
        this.name = name;
        this.following=false;
        this.url="";
    }
    public Category() {
        this.name = "";
        this.following=false;
        this.url="";
    }
    public Category(String name, String url, Boolean following){
        this.name=name;
        this.url=url;
        this.following=following;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }
}
