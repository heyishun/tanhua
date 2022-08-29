package com.tanhua.dubbo.api;

import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;

    /**
     * 上传视频
     * @param video
     * @return
     */
    @Override
    public String save(Video video) {
        //1.设置属性
        video.setVid(idWorker.getNextId("video"));
        video.setCreated(System.currentTimeMillis());
        //2.调用方法保存对象
        mongoTemplate.save(video);
        //3.返回
        return video.getId().toHexString();
    }

    /**
     * 根据vid查询视频列表
     * @param vids
     * @return
     */
    @Override
    public List<Video> findByVids(List<Long> vids) {
        Query query= Query.query(Criteria.where("vid").in(vids));
        return mongoTemplate.find(query,Video.class);
    }

    /**
     * 分页查询视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Video> queryVideoList(int page, Integer pagesize) {
        Query query=new Query().limit(pagesize).skip((page-1)*pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Video.class);
    }

    /**
     * 关注
     * @param focusUser
     */
    @Override
    public void focus(FocusUser focusUser) {
        //1.设置属性
        focusUser.setId(ObjectId.get());
        focusUser.setCreated(System.currentTimeMillis());
        //2.调用方法保存
        mongoTemplate.save(focusUser);
    }

    /**
     * 取消关注
     * @param uid
     * @param userId
     */
    @Override
    public void unFocus(Long uid, Long userId) {
        //1.构建Query对象
        Query query=Query.query(Criteria.where("userId").is(userId)
                .and("followUserId").is(uid));
        //2.调用方法删除
        mongoTemplate.remove(query,FocusUser.class);
    }

    @Override
    public Boolean hasFocus(Long userId, Long followUserId) {
        //1.构建Query对象
        Query query=Query.query(Criteria.where("userId").is(userId)
                .and("followUserId").is(followUserId));
        boolean exists = mongoTemplate.exists(query, FocusUser.class);
        if(exists){
            return true;
        }
        return false;
    }

    /**
     * 根据用户id查询视频列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findByUserId(Integer page, Integer pagesize, Long userId) {
            Query query=Query.query(Criteria.where("userId").is(userId));
            long count = mongoTemplate.count(query, Video.class);
            query.limit(pagesize).skip((page-1)*pagesize)
                    .with(Sort.by(Sort.Order.desc("created")));
            List<Video> list = mongoTemplate.find(query,Video.class);
            return new PageResult(page,pagesize,count,list);
    }
}