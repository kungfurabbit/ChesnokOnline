package Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexKarlov on 5/12/2017.
 */

public class Post {
    private String  post_id;
    private String  post_title;
    private String  post_date;
    List<Object> post_img = new ArrayList<Object>();

    private String  post_desc;

     public Post(){

     }

    public Post(List<Object> post_img) {
        this.post_img = post_img;
    }

    public List<Object> getPost_img() {
        return post_img;
    }

    public void setPost_img(List<Object> post_img) {
        this.post_img = post_img;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public String getPost_desc() {
        return post_desc;
    }

    public void setPost_desc(String post_desc) {
        this.post_desc = post_desc;
    }
}
