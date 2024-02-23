package com.boots.controller;

import com.boots.entity.User;
import com.boots.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {
    UserService userService;
    public AdminController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/admin")
    public String userList(Model model) {
        model.addAttribute("allUsers", userService.allUsers());  // Ключ, Значение
        return "admin";
    }

    @PostMapping("/admin")
    public String  deleteUser(@RequestParam(required = true, defaultValue = "" ) Long userId,
                              @RequestParam(required = true, defaultValue = "" ) String nickname,
                              @RequestParam(required = true, defaultValue = "" ) String username,
                              @RequestParam(required = true, defaultValue = "" ) String phoneNumber,
                              @RequestParam(required = true, defaultValue = "" ) String action,
                              Model model) {
        if (action.equals("delete")){
            userService.deleteUser(userId);
        }
        if (action.equals("update")){
            User user = userService.findUserById(userId);
            model.addAttribute("user", user);
            return "/user-update";
        }
        if (action.equals("update2")){
            User user = userService.findUserById(userId);
            userService.updateUser(user, nickname, username, phoneNumber);
        }

        return "redirect:/admin";
    }
    @GetMapping("/admin/gt/{userId}")
    public String  gtUser(@PathVariable("userId") Long userId, Model model) {
        model.addAttribute("allUsers", userService.usergtList(userId));
        return "admin";
    }
}
