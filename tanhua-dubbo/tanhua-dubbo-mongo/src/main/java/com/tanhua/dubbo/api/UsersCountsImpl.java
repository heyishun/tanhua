package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.mongo.UserLike;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DubboService
public class UsersCountsImpl implements UsersCounts{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Map<String,Integer> counts(Long userId) {
        //查询喜欢
        Query query=Query.query(Criteria.where("userId").is(userId).and("isLike").is(true));
        Integer loveCount = Math.toIntExact(mongoTemplate.count(query, UserLike.class));
        //相互喜欢
        Query query1=Query.query(Criteria.where("likeUserId").is(userId).and("isLike").is(true));
        List<UserLike> userLikes = mongoTemplate.find(query1, UserLike.class);
        List<UserLike> userLikes1 = mongoTemplate.find(query, UserLike.class);
        List<Long> ids = CollUtil.getFieldValues(userLikes, "userId", Long.class);
        List<Long> ids1 = CollUtil.getFieldValues(userLikes1, "likeUserId", Long.class);
        Integer eachLoveCount=0;
        for (Long id : ids) {
            for (Long aLong : ids1) {
                if(id==aLong){
                    eachLoveCount++;
                }
            }
        }
        Map<String,Integer> map=new HashMap<>();
        //粉丝
        Integer fanCount = Math.toIntExact(mongoTemplate.count(query1, UserLike.class));
        map.put("eachLoveCount",eachLoveCount);
        map.put("loveCount",loveCount);
        map.put("fanCount",fanCount);
        return map;
    }
}
