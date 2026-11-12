package com.huzaifa_khan.snapquestfyp;

public class Post {
    public String postId, userId, imageUrl, caption, date;
    public long likesCount, timestamp;

    public Post(){}

    public Post(String postId,String userId,String imageUrl,String caption,long likesCount){
        this.postId = postId;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likesCount = likesCount;
    }
}
