package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.UserMapper;
import com.tanhua.model.domain.User;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UserApiImpl implements UserApi{

    @Autowired
    private UserMapper userMapper;
    @Override
    //根据手机号码查询用户
    public User findByMobile(String mobile) {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("mobile",mobile);
        User user = userMapper.selectOne(queryWrapper);
        return user;
    }

    @Override
    //保存新用户
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updatePhone(String mobile, String phone) {
        User user = this.findByMobile(mobile);
        user.setMobile(phone);
        userMapper.updateById(user);
    }

    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    //根据id查询
    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 根据环信id查询用户详情
     * @param huanxinId
     * @return
     */
    @Override
    public User findByHuanxin(String huanxinId) {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("hx_user",huanxinId);
        User user = userMapper.selectOne(queryWrapper);
        return user;
    }

}
