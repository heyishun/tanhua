package com.tanhua.admin.service;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.exception.BusinessException;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.mapper.AdminMapper;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.Admin;
import com.tanhua.model.vo.AdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //用户登录
    public Map login(Map map) {
        //1、获取请求的参数（username,password,verificationCode（验证码）,uuid）
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String verificationCode = (String) map.get("verificationCode");
        String uuid = (String) map.get("uuid");
        //2.判断验证码是否正确
        String key=Constants.CAP_CODE+uuid;
        String value = redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(value)||!verificationCode.equals(value)){
            throw new BusinessException("验证码错误！");
        }
        redisTemplate.delete(key);
        //3.根据用户名查询管理员对象
        QueryWrapper<Admin> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("username",username);
        Admin admin = adminMapper.selectOne(queryWrapper);
        password = SecureUtil.md5(password);
        //4.判断账号密码
        if(admin==null||!password.equals(admin.getPassword())){
            throw new BusinessException("用户名或密码错误");
        }
        //6、通过JWT生成token
        Map tokenMap=new HashMap();
        tokenMap.put("id",admin.getId());
        tokenMap.put("username",admin.getUsername());
        String token = JwtUtils.getToken(tokenMap);
        //8、构造返回值
        Map retMap=new HashMap();
        retMap.put("token",token);
        return retMap;
    }

    //获取当前用户的用户资料
    public AdminVo profile() {
        Long id = AdminHolder.getUserId();
        Admin admin = adminMapper.selectById(id);
        return AdminVo.init(admin);
    }
}
