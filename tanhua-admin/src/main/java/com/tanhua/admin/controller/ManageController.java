package com.tanhua.admin.controller;

import com.tanhua.admin.service.ManagerService;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manage")
public class ManageController {
    @Autowired
    private ManagerService managerService;
    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = managerService.findAllUsers(page,pagesize);
        return ResponseEntity.ok(result);
    }
    /**
     * 根据id查询用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity findById(@PathVariable("userId") Long userId) {
        return managerService.findById(userId);
    }
    /**
     * 查询指定用户发布的所有视频列表
     */
    @GetMapping("/videos")
    public ResponseEntity<PageResult> videos(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer pagesize,
                                             Long uid ) {
        PageResult result = managerService.findAllVideos(page,pagesize,uid);
        return ResponseEntity.ok(result);
    }

    /**
     * 查看动态列表
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */
    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid,Integer state ) {
        PageResult result = managerService.findAllMovements(page,pagesize,uid,state);
        return ResponseEntity.ok(result);
    }

    /**
     * 单条动态查询
     */
    @GetMapping("/messages/{messageID}")
    public ResponseEntity findMovement(@PathVariable("messageID") String messageID) {
        MovementsVo vo = managerService.findMovement(messageID);
        return ResponseEntity.ok(vo);
    }

    /**
     * 动态评论集合
     * @return
     */
    @GetMapping("/messages/comments")
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   String messageID) {
        PageResult pageResult=managerService.findComments(page,pagesize,messageID);
        return ResponseEntity.ok(pageResult);
    }
}
