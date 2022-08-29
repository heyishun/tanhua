package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessagesController {

    @Autowired
    private MessagesService messagesService;

    /**
     * 根据环信用户id，查询用户详情
     * @param huanxinId
     * @return
     */
    @GetMapping("/userinfo")
    public ResponseEntity userinfo(String huanxinId) {
       UserInfoVo vo = messagesService.findUserInfoByHuanxin(huanxinId);
        return ResponseEntity.ok(vo);
    }

    /**
     * 添加好友
     * @param map
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity contacts(@RequestBody Map map){
        Long friendId = Long.valueOf(map.get("userId").toString());
        messagesService.contact(friendId);
        return ResponseEntity.ok(null);
    }
    /**
     * 分页查询联系人列表
     */
    @GetMapping("/contacts")
    public ResponseEntity contacts(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   String keyword) {
        PageResult pr = messagesService.findFriends(page,pagesize,keyword);
        return ResponseEntity.ok(pr);
    }

    /**
     * 查询点赞
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity findLikes(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pr=messagesService.findLikes(page,pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 评论查询
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pr=messagesService.findComments(page,pagesize);
        return ResponseEntity.ok(pr);
    }
    /**
     * 喜欢查询
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity findLoves(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pr=messagesService.findLoves(page,pagesize);
        return ResponseEntity.ok(pr);
    }
    
    /**
     * 公告查询
     * 敬请期待
     */
}