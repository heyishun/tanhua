package com.tanhua.server.controller;


import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserService userService;
    /**
     * 查询用户资料
     * 请求头 token
     * 请求参数 userID
     *
     */
    @GetMapping
    public ResponseEntity users(Long userID){

        //3.判断id
        if(userID==null){
            userID= UserHolder.getUserId();
        }
        UserInfoVo userInfoVo =userInfoService.findById(userID);
        return ResponseEntity.ok(userInfoVo);
    }

    /**
     * 更新用户资料
     * @return
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfo userInfo){


        //3.设置id
        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);
        return ResponseEntity.ok(null);
    }
    /**
     * 更新头像
     * @return
     */
    @PostMapping("/header")
    public ResponseEntity updateUserHeader(MultipartFile headPhoto) throws IOException {
        
        userInfoService.updateHead(headPhoto,UserHolder.getUserId());
        return ResponseEntity.ok(null);
    }
    /**
     * 发送验证码
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendMsg(){
        String mobile = UserHolder.getMobile();
        userService.sendMsg(mobile);
        return ResponseEntity.ok(null);
    }
    /**
     * 校验验证码
     */
    @PostMapping("/phone/checkVerificationCode")
    public ResponseEntity checkCode(@RequestBody Map map){
        String code = (String) map.get("verificationCode");
        String mobile = UserHolder.getMobile();
        boolean verification = userService.checkCode(code, mobile);
        return ResponseEntity.ok(verification);
    }
    /**
     * 保存新手机
     */
    @PostMapping("/phone")
    public ResponseEntity savaNewPhone(@RequestBody Map map){
        String phone = (String) map.get("phone");
        String mobile = UserHolder.getMobile();
        userService.update(mobile,phone);
        return ResponseEntity.ok(null);
    }

    /**
     * 相互喜欢，喜欢，粉丝
     * @return
     */
    @GetMapping("/counts")
    public ResponseEntity counts(){
        Map map =userInfoService.counts(UserHolder.getUserId());
        return ResponseEntity.ok(map);
    }
}
