package com.tanhua.server.service;

import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.UsersCounts;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoService {
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private UsersCounts usersCounts;
    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    public void save(UserInfo userInfo) {
        userInfoApi.save(userInfo);
    }

    //更新用户头像
    public void updateHead(MultipartFile headPhoto, Long id) throws IOException {
        //1.将图片上传到阿里云oss
        String imageUrl = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
        //2.调用百度云判断是否包含人脸
        boolean detect = aipFaceTemplate.detect(imageUrl);
        if(!detect){
            //2.1不含，抛出异常
            throw new BusinessException(ErrorResult.faceError());
        }else {
            //2.2含，调用api更新
            UserInfo userInfo=new UserInfo();
            userInfo.setId(Long.valueOf(id));
            userInfo.setAvatar(imageUrl);
            userInfoApi.update(userInfo);
        }
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public UserInfoVo findById(Long id) {
        UserInfo userInfo=userInfoApi.findById(id);
        UserInfoVo userInfoVo=new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);//同名属性
        if(userInfo.getAge()!=null){
            userInfoVo.setAge(userInfo.getAge().toString());
        }
        return userInfoVo;
    }

    /**
     * 更新用户信息
     * @param userInfo
     */
    public void update(UserInfo userInfo) {
        userInfoApi.update(userInfo);
    }

    /**
     * 互相喜欢，喜欢，粉丝
     * @return
     * @param userId
     */
    public Map counts(Long userId) {
        Map map=usersCounts.counts(userId);
        return map;
    }
}
