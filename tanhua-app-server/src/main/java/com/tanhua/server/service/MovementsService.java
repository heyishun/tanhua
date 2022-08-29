package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class MovementsService {

    @Autowired
    private OssTemplate ossTemplate;

    @DubboReference
    private MovementApi movementApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发布动态
     * @param movement
     * @param imageContent
     */

    public void publishMovement(Movement movement, MultipartFile[] imageContent) throws IOException {
        //1、判断发布动态的内容是否存在
        if(StringUtils.isEmpty(movement.getTextContent())) {
            throw  new BusinessException(ErrorResult.contentError());
        }
        //2、获取当前登录的用户id
        Long userId = UserHolder.getUserId();
        //3、将文件内容上传到阿里云OSS，获取请求地址
        List<String> medias = new ArrayList<>();
        for (MultipartFile multipartFile : imageContent) {
            String upload = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
            medias.add(upload);
        }
        //4、将数据封装到Movement对象
        movement.setUserId(userId);
        movement.setMedias(medias);
        //5、调用API完成发布动态
        movementApi.publish(movement);
    }

    //查询个人动态
    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        //1、根据用户id，调用API查询个人动态内容（PageResult  -- Movement）
        PageResult pr = movementApi.findByUserId(userId,page,pagesize);
        //2、获取PageResult中的item列表对象
        List<Movement> items = (List<Movement>) pr.getItems();
        //3、非空判断
        if(items == null) {
            return pr;
        }
        //4、循环数据列表
        UserInfo userInfo = userInfoApi.findById(userId);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : items) {
            //5、一个Movement构建一个Vo对象
            MovementsVo vo = MovementsVo.init(userInfo, item);
            vos.add(vo);
        }
        //6、构建返回值
        pr.setItems(vos);
        return pr;
    }

    //好友动态
    public PageResult findFriendMovements(Integer page, Integer pagesize) {
        //1、获取当前用户id
        Long userId = UserHolder.getUserId();
        //2、查询数据列表
        List<Movement> items = movementApi.findFriendMovements(userId,page,pagesize);
        //3、非空判断
        if(CollUtil.isEmpty(items)) {
            return new PageResult();
        }
        //4、获取好友用户id
        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        //5、循环数据列表
        Map<Long, UserInfo> userMaps = userInfoApi.findByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : items) {
            //5、一个Movement构建一个Vo对象
            UserInfo userInfo = userMaps.get(item.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, item);
            //修复点赞状态bug，判断hashKey是否存在
            String key = Constants.MOVEMENTS_INTERACT_KEY + item.getId().toHexString();
            String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
            if(redisTemplate.opsForHash().hasKey(key,hashKey)){
                vo.setHasLiked(1);
            }
            String lovehashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
            if(redisTemplate.opsForHash().hasKey(key,lovehashKey)){
                vo.setHasLoved(1);
            }
            vos.add(vo);
        }
        //6、构建返回值
        return new PageResult(page,pagesize,0L,vos);
    }

    //查询推荐动态
    public PageResult findRecommendMovements(Integer page, Integer pagesize) {
        String redisKey = Constants.MOVEMENTS_RECOMMEND + UserHolder.getUserId();
        String redisData = this.redisTemplate.opsForValue().get(redisKey);
        List<Movement> list = Collections.EMPTY_LIST;
        if(StringUtils.isEmpty(redisData)){
            list = movementApi.randomMovements(pagesize);
        }else {
            String[] split = redisData.split(",");
            if ((page-1) * pagesize > split.length) {
                return new PageResult();
            }
            List<Long> pids = Arrays.stream(split)
                    .skip((page - 1) * pagesize)
                    .limit(pagesize)
                    .map(e -> Convert.toLong(e))
                    .collect(Collectors.toList());
            list = movementApi.findByPids(pids);
        }
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        //5、循环数据列表
        Map<Long, UserInfo> userMaps = userInfoApi.findByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : list) {
            //5、一个Movement构建一个Vo对象
            UserInfo userInfo = userMaps.get(item.getUserId());
            MovementsVo vo = MovementsVo.init(userInfo, item);
            //修复点赞状态bug，判断hashKey是否存在
            String key = Constants.MOVEMENTS_INTERACT_KEY + item.getId().toHexString();
            String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
            if(redisTemplate.opsForHash().hasKey(key,hashKey)){
                vo.setHasLiked(1);
            }
            String lovehashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
            if(redisTemplate.opsForHash().hasKey(key,lovehashKey)){
                vo.setHasLoved(1);
            }
            vos.add(vo);
        }
        //6、构建返回值
        return new PageResult(page,pagesize,0L,vos);
    }

    /**
     * 单条动态
     * @param movementId
     * @return
     */
    public MovementsVo findMovementById(String movementId) {
        Movement movement = movementApi.findById(movementId);
        if (movement != null) {
            UserInfo userInfo = userInfoApi.findById(movement.getUserId());
            return MovementsVo.init(userInfo,movement);
        }
        else {
            return null;
        }
    }

    @DubboReference
    private VisitorsApi visitorsApi;
    /**
     * 首页访客列表
     * @return
     */
    public List<VisitorsVo> queryVisitorsList() {
        //1.查询访问时间
        String key=Constants.VISITORS_USER;
        String hashKey=String.valueOf(UserHolder.getUserId());
        String value= (String) redisTemplate.opsForHash().get(key,hashKey);
        Long date=StringUtils.isEmpty(value)? null:Long.valueOf(value);
        //2.调用api查询数据列表List<Visitor>
        List<Visitors> list=visitorsApi.queryMyVisitors(date,UserHolder.getUserId());
        if(CollUtil.isEmpty(list)){
        return new ArrayList<>();
        }
        //3.提取用户的id
        List<Long> userIds = CollUtil.getFieldValues(list, "visitorUserId", Long.class);
        //4.查看用户详情
        Map<Long, UserInfo> userInfoMap = userInfoApi.findByIds(userIds, null);
        //5.构造返回
        List<VisitorsVo> vos=new ArrayList<>();
        for (Visitors visitors : list) {
            UserInfo userInfo = userInfoMap.get(visitors.getVisitorUserId());
            if(userInfo!=null){
                VisitorsVo vo = VisitorsVo.init(userInfo, visitors);
                vos.add(vo);
            }
        }
        return vos;
    }
}