package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
    
@Entity
public class Likes {
    
    @Id
    private String shortId;

    private String ownerId; 

    @Id
    private String userId;
    
    public Likes () {}
    
    public Likes(String shortId,String ownerId, String userId) {
        this.shortId = shortId;
        this.ownerId = ownerId;
        this.userId = userId;
    }
    
    public String getShortLiked(){
        return shortId;
    }
    
    public String getUser(){
        return userId;
    }

    public String getOwnerId(){
        return ownerId;
    }
    
    public void setOwnerId(String ownerId){
        this.ownerId = ownerId;
    }
    
    public void setShortLiked(String shortId){
        this.shortId = shortId;
    }
    
    public void serUser(String userId){
        this.userId = userId;
    }

}    
