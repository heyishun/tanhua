package com.tanhua.dubbo.api;

import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Friend;

import java.util.List;

public interface FriendApi {
    //添加好友
    void save(Long userId, Long friendId);

    //查询好友列表
    List<Friend> findByUserId(Long userId, Integer page, Integer pagesize);

    //查询点赞
    List<Comment> findLikes(CommentType like, Long userId, Integer page, Integer pagesize);

    //查询评论
    List<Comment> findComments(CommentType comment, Long userId, Integer page, Integer pagesize);

    //查询喜欢
    List<Comment> findLoves(CommentType love, Long userId, Integer page, Integer pagesize);
}
