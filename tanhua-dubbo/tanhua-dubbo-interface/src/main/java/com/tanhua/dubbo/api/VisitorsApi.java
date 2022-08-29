package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;

import java.util.List;

public interface VisitorsApi {

    /**
     * 保存访客数据
     */
    void save(Visitors visitors);


    /**
     * 查看首页访客列表
     * @param date
     * @param userId
     * @return
     */
    List<Visitors> queryMyVisitors(Long date, Long userId);
}
