package com.tanhua.dubbo.api;

import com.tanhua.model.domain.User;

public interface UserApi {
    //根据手机号码查询用户
    User findByMobile(String mobile);

    //保存用户，返回id
    Long save(User user);

    //更新手机号
    void updatePhone(String mobile, String phone);

    //更新
    void update(User user);

    //根据id查询
    User findById(Long userId);

    //根据环信id查询用户详情
    User findByHuanxin(String huanxinId);
}
