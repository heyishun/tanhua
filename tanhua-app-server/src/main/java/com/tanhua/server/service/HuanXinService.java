package com.tanhua.server.service;

import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.vo.HuanXinUserVo;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class HuanXinService {
    @DubboReference
    private UserApi userApi;
    /**
     * 查询当前用户的环信账号
     * @return
     */
    public HuanXinUserVo findHuanXinUser() {
        Long userId = UserHolder.getUserId();
        User user =userApi.findById(userId);
        if(user==null){
            return null;
        }
        return new HuanXinUserVo(user.getHxUser(),user.getHxPassword());
    }
}
