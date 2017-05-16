package Model;

/**
 * Created by AlexKarlov on 5/15/2017.
 */

public class Comment {

    private String comment_text;
    private String user_id;

    public Comment(){}

    public Comment(String comment_text, String user_id) {
        this.comment_text = comment_text;
        this.user_id = user_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void getList(){

    }
}
