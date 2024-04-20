package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follow {

    @Id
    private String userFollowing;
  
    @Id
    private String userFollowed;

    public Follow () {}

    public Follow(String userFollowing, String userFollowed) {
        this.userFollowed = userFollowed;
        this.userFollowing = userFollowing;
    }

    public String getUserFollowing(){
        return userFollowing;
    }

    public String getUserFollowed(){
        return userFollowed;
    }

    public void setUserFollowing(String userFollowing){
        this.userFollowing = userFollowing;
    }

    public void serUserFollowed(String userFollowed){
        this.userFollowed = userFollowed;
    }
}
