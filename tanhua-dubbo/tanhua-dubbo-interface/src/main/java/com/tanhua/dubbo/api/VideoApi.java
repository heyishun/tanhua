package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface VideoApi {

    //上传视频
    String save(Video video);

    //根据vid查询视频列表
    List<Video> findByVids(List<Long> vids);

    //分页查询视频列表
    List<Video> queryVideoList(int page, Integer pagesize);

    //关注
    void focus(FocusUser focusUser);

    //取消关注
    void unFocus(Long uid, Long userId);

    //查看是否关注
    Boolean hasFocus(Long userId, Long followUserId);


    //根据用户id查询视频列表
    PageResult findByUserId(Integer page, Integer pagesize, Long userId);
}