package com.tanhua.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManagerService {
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private VideoApi videoApi;
    @DubboReference
    private MovementApi movementApi;
    @DubboReference
    private CommentApi commentApi;

    public PageResult findAllUsers(Integer page, Integer pagesize) {
        IPage iPage = userInfoApi.findAll(page, pagesize);
        return new PageResult(page,pagesize,iPage.getTotal(),iPage.getRecords());
    }

    //根据id查询用户详情
    public ResponseEntity findById(Long userId) {
        UserInfo info = userInfoApi.findById(userId);
        return ResponseEntity.ok(info);
    }

    //根据用户id分页查询此用户发布的所有视频列表
    public PageResult findAllVideos(Integer page, Integer pagesize, Long userId) {
        return videoApi.findByUserId(page,pagesize,userId);
    }

    //查询指定用户发布的所有动态
    public PageResult findAllMovements(Integer page, Integer pagesize, Long userId, Integer state) {
        //1、调用API查询 ：（PageResult<Publish>）
        PageResult result = movementApi.findByUserId(userId,page,pagesize);
        //2、一个Publsh构造一个Movements
        List<Movement> items = ( List<Movement>)result.getItems();
        List<MovementsVo> list = new ArrayList<>();
        for (Movement item : items) {
            UserInfo userInfo = userInfoApi.findById(item.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, item);
            list.add(vo);
        }
        //3、构造返回值
        result.setItems(list);
        return result;
    }


    /**
     * 单条动态查询
     * @param mid
     * @return
     */
    public MovementsVo findMovement(String mid) {
        Movement movement = movementApi.findById(mid);
        UserInfo userInfo = userInfoApi.findById(movement.getUserId());
        MovementsVo vo = MovementsVo.init(userInfo, movement);
        vo.setState(movement.getState());
        return vo;
    }
    /**
     * 动态评论集合
     *
     */
    public PageResult findComments(Integer page,Integer pagesize,String messageId) {
        //调用API查询
        PageResult result=commentApi.findAll(messageId, CommentType.COMMENT, page, pagesize);
        return result;
    }
}
