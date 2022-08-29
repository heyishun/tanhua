package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.PageUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.FocusUser;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VideoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SmallVideosService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer webServer;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 上传视频
     * @param videoThumbnail 封面图片
     * @param videoFile 视频文件
     */
    public void saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        if(videoFile.isEmpty()||videoThumbnail.isEmpty()){
            throw new BusinessException(ErrorResult.error());
        }
        //1.将视频上传到FastDFS，获取访问url
        String filename = videoFile.getOriginalFilename();  //ssss.avi
        filename = filename.substring(filename.lastIndexOf(".")+1);
        StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), filename, null);
        String videoUrl=webServer.getWebServerUrl()+storePath.getFullPath();
        //2.将封面图片上传到阿里云OSS，获取访问呢url
        String imageUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        //3.构建Videos对象
        Video video=new Video();
        video.setUserId(UserHolder.getUserId());
        video.setPicUrl(imageUrl);
        video.setVideoUrl(videoUrl);
        video.setText("我就是我，不一样的烟火~");
        //4.调用API保存数据
        String videoId=videoApi.save(video);
        if(StringUtils.isEmpty(videoId)){
            throw new BusinessException(ErrorResult.error());
        }
    }

    /**
     * 查询视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @Cacheable(
            value = "videos",
            key = "T(com.tanhua.server.interceptor.UserHolder).getUserId()+'_'+#page+'_'+#pagesize")//userid_page_pagesize
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        //1.查询redis数据
        String redisKey = Constants.VIDEOS_RECOMMEND + UserHolder.getUserId();
        String redisValue = this.redisTemplate.opsForValue().get(redisKey);
        //2.判断redis数据是否存在，判断redis数据是否满足本册分页的条数
        List<Video> list=new ArrayList<>();
        int redisPages=0;
        if(!StringUtils.isEmpty(redisValue)){
            //3.如果redis存在数据，根据VID查询数据
            String[] split = redisValue.split(",");
            //判断当前页的起始条数是否小于数组总数
            if ((page-1) * pagesize < split.length) {
                List<Long> vids = Arrays.stream(split)
                        .skip((page - 1) * pagesize)
                        .limit(pagesize)
                        .map(e -> Convert.toLong(e))
                        .collect(Collectors.toList());
                list = videoApi.findByVids(vids);
                redisPages= PageUtil.totalPage(split.length,pagesize);
            }
        }

        //4.如果redis数据不存在，分页查询视频数据
        if(list.isEmpty()){
            list=videoApi.queryVideoList(page-redisPages,pagesize);
        }
        //5.提取视频列表中所有用户的id
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        //6.查询用户信息
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds,null);
        //7.构建返回值
        List<VideoVo> vos=new ArrayList<>();
        for (Video video : list) {
            UserInfo userInfo = map.get(video.getUserId());
            if(userInfo!=null){
                VideoVo videoVo = VideoVo.init(userInfo, video);
                //查看是否关注
                /*Boolean flag=videoApi.hasFocus(UserHolder.getUserId(),video.getUserId());
                if(flag){
                    videoVo.setHasFocus(1);
                }*/
                String key = Constants.FOCUS_USER + UserHolder.getUserId();
                String hashKey= String.valueOf(video.getUserId());
                if(redisTemplate.opsForHash().hasKey(key,hashKey)){
                    videoVo.setHasFocus(1);
                }
                vos.add(videoVo);
            }
        }
        return new PageResult(page,pagesize,0l,vos);
    }

    /**
     * 关注用户
     * @param uid
     */
    public void focus(Long uid) {
        //1.获取操作用户id
        Long userId = UserHolder.getUserId();
        //2.创建对象
        FocusUser focusUser=new FocusUser();
        focusUser.setUserId(userId);
        focusUser.setFollowUserId(uid);
        //调用API保存
        videoApi.focus(focusUser);
        //3、将关注记录存入redis中
        String key = Constants.FOCUS_USER + UserHolder.getUserId();
        String hashKey = String.valueOf(uid);
        redisTemplate.opsForHash().put(key,hashKey,"1");
    }

    /**
     * 取消关注
     * @param uid
     */
    public void unFocus(Long uid) {
        Long userId = UserHolder.getUserId();
        videoApi.unFocus(uid,userId);
        String key = Constants.FOCUS_USER + UserHolder.getUserId();
        String hashKey = String.valueOf(uid);
        redisTemplate.opsForHash().delete(key,hashKey);
    }
}