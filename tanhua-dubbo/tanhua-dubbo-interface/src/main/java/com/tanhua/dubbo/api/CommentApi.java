package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface CommentApi {
    //发表评论
    Integer save(Comment comment1);
    //分页查询
    List<Comment> findComments(String movementId, CommentType comment, Integer page, Integer pagesize);
    //是否点赞
    Boolean hasComment(String movementId, Long userId, CommentType like);
    //取消点赞
    Integer delete(Comment comment);
    //评论点赞
    Integer hasMComment(String commendId);
    //评论取消点赞
    Integer disHasMComment(String commendId);

    //分页查询
    PageResult findAll(String messageId, CommentType comment, Integer page, Integer pagesize);
}
