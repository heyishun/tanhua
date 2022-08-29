package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.FriendApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.vo.*;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessagesService {

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private FriendApi friendApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 根据环信id查询用户详情
     */
    public UserInfoVo findUserInfoByHuanxin(String huanxinId) {
        //1、根据环信id查询用户
        User user = userApi.findByHuanxin(huanxinId);
        //2、根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(user.getId());
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo,vo); //copy同名同类型的属性
        if(userInfo.getAge() != null) {
            vo.setAge(userInfo.getAge().toString());
        }
        return vo;
    }

    /**
     * 添加好友关系
     * @param friendId
     */
    public void contact(Long friendId) {
        //1.将好友关系注册到环信
        Boolean aBoolean = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + UserHolder.getUserId()
                , Constants.HX_USER_PREFIX + friendId);

        if(!aBoolean){
            throw new BusinessException(ErrorResult.error());
        }
        //2.如果注册成功，记录到MongoDB
        friendApi.save(UserHolder.getUserId(),friendId);

    }

    /**\
     * 分页查询联系人列表
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    public PageResult findFriends(Integer page, Integer pagesize, String keyword) {
        //1、调用API查询当前用户的好友数据 -- List<Friend>
        List<Friend> list = friendApi.findByUserId(UserHolder.getUserId(),page,pagesize);
        if(CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        //2、提取数据列表中的好友id
        List<Long> userIds = CollUtil.getFieldValues(list, "friendId", Long.class);
        //3、调用UserInfoAPI查询好友的用户详情
        UserInfo info = new UserInfo();
        info.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, info);
        //4、构造VO对象
        List<ContactVo> vos = new ArrayList<>();
        for (Friend friend : list) {
            UserInfo userInfo = map.get(friend.getFriendId());
            if(userInfo != null) {
                ContactVo vo = ContactVo.init(userInfo);
                vos.add(vo);
            }
        }
        return new PageResult(page,pagesize,0l,vos);
    }


    /**
     * 点赞列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findLikes(Integer page, Integer pagesize) {
        //1.得到当前用户Id
        Long userId = UserHolder.getUserId();
        //2.查询当前用户点赞列表
        List<Comment> list=friendApi.findLikes(CommentType.LIKE,userId,page,pagesize);
        if(CollUtil.isEmpty(list)){
            return new PageResult();
        }
        //3.提取点赞人的id
        List<Long> publishUserIds = CollUtil.getFieldValues(list, "publishUserId",Long.class);
        //4.查询点赞人map集合
        Map<Long, UserInfo> map = userInfoApi.findByIds(publishUserIds,null);
        //5.构造返回值VO
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            CommentVo vo=new CommentVo();
            //1.设置id
            vo.setId(comment.getId().toString());
            //得到点赞人id
            Long publishUserId = comment.getPublishUserId();
            //map集合中点赞人信息
            UserInfo userInfo = map.get(publishUserId);
            //2.设置头像
            vo.setAvatar(userInfo.getAvatar());
            //设置返回昵称
            vo.setNickname(userInfo.getNickname());
            //3.设置时间
            //时间戳转为日期
            vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(comment.getCreated()));
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0l,vos);
    }

    /**
     * 评论查询
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findComments(Integer page, Integer pagesize) {
        //1.得到当前用户Id
        Long userId = UserHolder.getUserId();
        //2.查询当前用户点赞列表
        List<Comment> list=friendApi.findComments(CommentType.COMMENT,userId,page,pagesize);
        if(CollUtil.isEmpty(list)){
            return new PageResult();
        }
        //3.提取点赞人的id
        List<Long> publishUserIds = CollUtil.getFieldValues(list, "publishUserId",Long.class);
        //4.查询点赞人map集合
        Map<Long, UserInfo> map = userInfoApi.findByIds(publishUserIds,null);
        //5.构造返回值VO
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            CommentVo vo=new CommentVo();
            //1.设置id
            vo.setId(comment.getId().toString());
            //得到点赞人id
            Long publishUserId = comment.getPublishUserId();
            //map集合中点赞人信息
            UserInfo userInfo = map.get(publishUserId);
            //2.设置头像
            vo.setAvatar(userInfo.getAvatar());
            //设置返回昵称
            vo.setNickname(userInfo.getNickname());
            //3.设置时间
            //时间戳转为日期
            vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(comment.getCreated()));
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0l,vos);
    }

    /**
     * 查询喜欢
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findLoves(Integer page, Integer pagesize) {
        //1.得到当前用户Id
        Long userId = UserHolder.getUserId();
        //2.查询当前用户点赞列表
        List<Comment> list=friendApi.findLoves(CommentType.LOVE,userId,page,pagesize);
        if(CollUtil.isEmpty(list)){
            return new PageResult();
        }
        //3.提取点赞人的id
        List<Long> publishUserIds = CollUtil.getFieldValues(list, "publishUserId",Long.class);
        //4.查询点赞人map集合
        Map<Long, UserInfo> map = userInfoApi.findByIds(publishUserIds,null);
        //5.构造返回值VO
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            CommentVo vo=new CommentVo();
            //1.设置id
            vo.setId(comment.getId().toString());
            //得到点赞人id
            Long publishUserId = comment.getPublishUserId();
            //map集合中点赞人信息
            UserInfo userInfo = map.get(publishUserId);
            //2.设置头像
            vo.setAvatar(userInfo.getAvatar());
            //设置返回昵称
            vo.setNickname(userInfo.getNickname());
            //3.设置时间
            //时间戳转为日期
            vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(comment.getCreated()));
            vos.add(vo);
        }
        return new PageResult(page,pagesize,0l,vos);
    }

}