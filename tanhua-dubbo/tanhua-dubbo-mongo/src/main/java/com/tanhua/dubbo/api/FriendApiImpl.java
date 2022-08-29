package com.tanhua.dubbo.api;

import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Friend;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class FriendApiImpl implements FriendApi{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加好友
     * @param userId
     * @param friendId
     */
    @Override
    public void save(Long userId, Long friendId) {
        //1. 保存自己的好友数据
        Query query1 = Query.query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        //1.1 好友关系是否存在
        if(!mongoTemplate.exists(query1, Friend.class)){
            //1.2 如果不存在，保存
            Friend friend1 =new Friend();
            friend1.setUserId(userId);
            friend1.setFriendId(friendId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
        //2. 保存好友的数据
        Query query2 = Query.query(Criteria.where("userId").is(friendId).and("friendId").is(userId));
        //2.1 判断好友关系是否存在
        if(!mongoTemplate.exists(query2, Friend.class)){
            //2.2 如果不存在，保存
            Friend friend1 =new Friend();
            friend1.setUserId(friendId);
            friend1.setFriendId(userId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
    }

    /**
     * 查询好友列表
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Friend> findByUserId(Long userId, Integer page, Integer pagesize) {
        Query query=Query.query(Criteria.where("userId").is(userId))
                .skip((page-1)*pagesize).
                limit(pagesize).with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Friend.class);
    }

    /**
     * 查看点赞
     * @param like
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Comment> findLikes(CommentType like, Long userId, Integer page, Integer pagesize) {
        Query query=Query.query(Criteria.where("userId").is(userId).and("commentType").is(like.getType()))
                .skip((page-1)*pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Comment.class);
    }

    /**
     * 查看评论
     * @param comment
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Comment> findComments(CommentType comment, Long userId, Integer page, Integer pagesize) {
        Query query=Query.query(Criteria.where("userId").is(userId).and("commentType").is(comment.getType()))
                .skip((page-1)*pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Comment.class);
    }

    /**
     * 查询喜欢
     * @param love
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Comment> findLoves(CommentType love, Long userId, Integer page, Integer pagesize) {
        Query query=Query.query(Criteria.where("userId").is(userId).and("commentType").is(love.getType()))
                .skip((page-1)*pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Comment.class);
    }
}
