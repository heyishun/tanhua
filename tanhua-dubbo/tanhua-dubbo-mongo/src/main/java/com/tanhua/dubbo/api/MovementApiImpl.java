package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.TimeLineService;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.MovementTimeLine;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class MovementApiImpl implements MovementApi{
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TimeLineService timeLineService;

    /**
     * 发布动态
     * @param movement
     */
    @Override
    public void publish(Movement movement) {
        try {
            //1.保存动态详情
            //设置pid
            movement.setPid(idWorker.getNextId("movement"));
            //设置时间
            movement.setCreated(System.currentTimeMillis());
            mongoTemplate.save(movement);
            /*//2、查询当前用户的好友数据
            Criteria criteria=Criteria.where("userId").is(movement.getUserId());
            Query query=Query.query(criteria);
            List<Friend> friends = mongoTemplate.find(query, Friend.class);
            //3、循环好友数据，构建时间线数据存入数据库
            for (Friend friend : friends) {
                MovementTimeLine timeLine = new MovementTimeLine();
                timeLine.setMovementId(movement.getId());
                timeLine.setUserId(friend.getUserId());
                timeLine.setFriendId(friend.getFriendId());
                timeLine.setCreated(System.currentTimeMillis());
                mongoTemplate.save(timeLine);
            }*/
            timeLineService.saveTimeLine(movement.getUserId(),movement.getId());
        } catch (Exception e) {
            //忽略事务
            e.printStackTrace();
        }
    }

    @Override
    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        Criteria criteria=Criteria.where("userId").is(userId);
        Query query=Query.query(criteria).skip((page-1)*pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return new PageResult(page,pagesize,0l,movements);

    }

    /**
     * 查询当前用户好友发布的动态
     * @param friendId 当前操作用户的id
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> findFriendMovements(Long friendId, Integer page, Integer pagesize) {
        //1、查询好友时间线表
        Query query = Query.query(Criteria.where("friendId").in(friendId))
                .skip((page - 1)*pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<MovementTimeLine> lines = mongoTemplate.find(query, MovementTimeLine.class);
        //2、提取动态id集合
        List<ObjectId> movementIds = CollUtil.getFieldValues(lines, "movementId", ObjectId.class);
        //3、根据动态id查询动态详情
        Query movementQuery = Query.query(Criteria.where("id").in(movementIds));
        return mongoTemplate.find(movementQuery, Movement.class);
    }

    /**
     * 随机查询多条数据
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> randomMovements(Integer pagesize) {
        //1.创建统计对象，设置统计参数
        TypedAggregation aggregation = Aggregation.newAggregation(Movement.class,
                Aggregation.sample(pagesize));
        //2.调用mongoTemplate
        AggregationResults<Movement> movements = mongoTemplate.aggregate(aggregation,Movement.class);
        //3.获取统计结果
        return movements.getMappedResults();
    }

    /**
     * 根据pid查询
     * @param pids
     * @return
     */
    @Override
    public List<Movement> findByPids(List<Long> pids) {
        Query query=Query.query(Criteria.where("pid").in(pids));
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return movements;
    }

    /**
     * 查询单条动态
     * @param movementId
     * @return
     */
    @Override
    public Movement findById(String movementId) {
        return mongoTemplate.findById(movementId,Movement.class);
    }
}
