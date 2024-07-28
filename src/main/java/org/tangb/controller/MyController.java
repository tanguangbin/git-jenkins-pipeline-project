package org.tangb.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.tangb.modules.User;
import org.tangb.service.IUserService;

import java.util.List;

@RestController
public class MyController {
    @Value("${tangb:Hello default}")
    private String tangb;

    private final IUserService userService;

    public MyController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/userList")
    public List<User> userList(){
        return userService.list();
    }

    @PostMapping("/userListByLike")
    public List<User> userListByLike(@RequestBody User user){
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.like(User::getId,user.getUsername());
//        return userService.list(wrapper);
        return userService.lambdaQuery().like(User::getId,user.getUsername()).list();
    }
    @GetMapping("/getUserById/{id}")
    public User getUserById(@PathVariable int id){
       return userService.getById(id);
    }

    @PutMapping("/updateUser")
    public boolean updateUser(@RequestBody User user){
        return userService.updateById(user);
    }

    @PutMapping("/lambdaUpdateUser")
    public boolean lambdaUpdateUser(@RequestBody User user){
        return userService.lambdaUpdate()
                .set(user.getUsername().equals("admin"),User::getNickname,user.getNickname()+"123")
//                .eq(User::getUsername,"admin")
                .update();

    }

    @PutMapping("/saveOrUpdateUserBatch")
    public boolean saveOrUpdateUserBatch(@RequestBody List<User> user){
        return userService.saveOrUpdateBatch(user);
    }

    @PostMapping("/addUser")
    public boolean addUser(@RequestBody User user){
        return userService.save(user);
    }


    @DeleteMapping("/deleteUser/{id}")
    public boolean deleteUser(@PathVariable String id){
        return userService.removeById(id);
    }

    @DeleteMapping("/deleteBatch")
    public boolean deleteUserBatch(@RequestBody List<User> userList){
        return userService.removeBatchByIds(userList);
    }
}
