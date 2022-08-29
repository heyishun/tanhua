package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

@DubboService
public class CommentApiImpl implements CommentApi{
    @Autowired
    private MongoTemplate mongoTemplate;
    @DubboReference
    private UserInfoApi userInfoApi;

    @Override
    public Integer save(Comment comment1) {
        //1.查询动态
        Movement movement = mongoTemplate.findById(comment1.getPublishId(), Movement.class);
        //2.向Comment对象设置评论人属性
        if(movement!=null){
            comment1.setPublishUserId(movement.getUserId());
        }
        //3.保存到数据库
        mongoTemplate.save(comment1);
        //4.更新动态表中的对应字段
        Query query=Query.query(Criteria.where("id").is(comment1.getPublishId()));
        Update update=new Update();
        if(comment1.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",1);
        }else if (comment1.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",1);
        }else {
            update.inc("loveCount",1);
        }
        //设置更新参数
        FindAndModifyOptions options=new FindAndModifyOptions();
        options.returnNew(true);
        Movement modify = mongoTemplate.findAndModify(query, update, options, Movement.class);
        //5.获取最新的评论数量
        return modify.statisCount(comment1.getCommentType());
    }

    //分页查询
    @Override
    public List<Comment> findComments(String movementId, CommentType commentType, Integer page, Integer pagesize) {
        //1.构造查询条件
        Query query=Query.query(Criteria.where("publishId").is(new ObjectId(movementId)).and("commentType")
                .is(commentType.getType()))
                .skip((page-1)*pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        //2.查询并返回
        return mongoTemplate.find(query,Comment.class);
    }

    /**
     * 查询动态是否点赞
     * @param movementId
     * @param userId
     * @param
     * @return
     */
    //判断comment数据是否存在
    public Boolean hasComment(String movementId, Long userId, CommentType commentType) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(movementId))
                .and("commentType").is(commentType.getType());
        Query query = Query.query(criteria);
        return mongoTemplate.exists(query,Comment.class); //判断数据是否存在
    }

    /**
     * 取消点赞与喜欢
     * @param comment
     * @return
     */
    @Override
    public Integer delete(Comment comment) {
        //1、删除Comment表数据
        Criteria criteria = Criteria.where("userId").is(comment.getUserId())
                .and("publishId").is(comment.getPublishId())
                .and("commentType").is(comment.getCommentType());
        Query query = Query.query(criteria);
        mongoTemplate.remove(query,Comment.class);
        //2、修改动态表中的总数量
        Query movementQuery = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        if(comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",-1);
        }else if (comment.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",-1);
        }else {
            update.inc("loveCount",-1);
        }
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true) ;//获取更新后的最新数据
        Movement modify = mongoTemplate.findAndModify(movementQuery, update, options, Movement.class);
        //5、获取最新的评论数量，并返回
        return modify.statisCount(comment.getCommentType() );
    }

    /**
     * 评论点赞
     * @param commendId
     * @return
     */
    @Override
    public Integer hasMComment(String commendId) {
        Query movementQuery = Query.query(Criteria.where("_id").is(commendId));
        Update update = new Update();
        update.inc("likeCount",1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true) ;//获取更新后的最新数据
        Comment modify = mongoTemplate.findAndModify(movementQuery, update, options, Comment.class);
        //获取最新的评论数量，并返回
        return modify.getLikeCount();
    }

    /**
     * 评论取消点赞
     * @param commendId
     * @return
     */
    @Override
    public Integer disHasMComment(String commendId) {
        Query movementQuery = Query.query(Criteria.where("_id").is(commendId));
        Update update = new Update();
        update.inc("likeCount",-1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true) ;//获取更新后的最新数据
        Comment modify = mongoTemplate.findAndModify(movementQuery, update, options, Comment.class);
        //获取最新的评论数量，并返回
        return modify.getLikeCount();
    }

    /**
     * 分页查询评论列表
     * @param messageId
     * @param comment
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findAll(String messageId, CommentType type, Integer page, Integer pagesize) {
        Query query=Query.query(Criteria.where("publishId").is(new ObjectId(messageId)).and("commentType").is(type.getType()));
        long count = mongoTemplate.count(query,Comment.class);
        query.skip((page-1)*pagesize).limit(pagesize).with(Sort.by(Sort.Order.desc("created")));
        List<Comment> list = mongoTemplate.find(query, Comment.class);
        List<CommentVo> vos=new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = userInfoApi.findById(comment.getUserId());
            CommentVo vo = CommentVo.init(userInfo, comment);
            vos.add(vo);
        }
        return new PageResult(page,pagesize,count,vos);
    }
}
