package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface MovementApi {

    //发布动态
    void publish(Movement movement);

    //根据用户id查询此用户发布的动态数据列表
    PageResult findByUserId(Long userId, Integer page, Integer pagesize);

    //好友动态
    List<Movement> findFriendMovements(Long userId, Integer page, Integer pagesize);
    //随机获取多条动态数据
    List<Movement> randomMovements(Integer pagesize);
    //根据pid数组查询动态
    List<Movement> findByPids(List<Long> pids);

    //查询单条动态
    Movement findById(String movementId);
}