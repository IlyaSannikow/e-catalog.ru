package com.boots.controller;

import com.boots.entity.Group;
import com.boots.entity.User;
import com.boots.service.GroupService;
import com.boots.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Controller
public class GroupController {

    GroupService groupService;
    UserService userService;

    public GroupController(GroupService groupService, UserService userService){
        this.groupService = groupService;
        this.userService = userService;
    }

    @GetMapping("/groupList")
    public String groupList( Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("userId", authentication.getName());

        model.addAttribute("groupForm", new Group()); // Ключ, Значение
        model.addAttribute("allGroup", groupService.allGroup()); // Ключ, Значение
        return "groupList";
    }

    @PostMapping("/groupList")
    public String  deleteProduct(@ModelAttribute("groupForm") @Valid Group groupForm,
                                 @RequestParam(required = true, defaultValue = "" ) Long groupId,
                                 @RequestParam(required = true, defaultValue = "" ) Long userId,
                                 @RequestParam(required = true, defaultValue = "" ) String name,
                                 @RequestParam(required = true, defaultValue = "" ) String action,
                                 BindingResult bindingResult,
                                 Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        if (action.equals("create")){
            if (bindingResult.hasErrors()) {
                return "groupList";
            }
            groupService.saveGroup(groupForm);

            return "redirect:/groupList";
        }

        if (action.equals("delete")){
            groupService.deleteGroup(groupId);
        }

        if (action.equals("add")){
            Group groupFromBD = groupService.findGroupById(groupId);
            User userFromBD = userService.findUserByUsername(currentPrincipalName);
            groupService.addGroupUsers(groupFromBD, userFromBD);
        }

        if (action.equals("remove")){
            Group groupFromBD = groupService.findGroupById(groupId);
            User userFromBD = userService.findUserByUsername(currentPrincipalName);
            groupService.removeGroupUsers(groupFromBD, userFromBD);
        }

        if (action.equals("update")){
            Group group = groupService.findGroupById(groupId);
            model.addAttribute("group", group);
            return "/group-update";
        }

        if (action.equals("update2")){
            Group group = groupService.findGroupById(groupId);
            groupService.updateGroup(group, name);
        }

        return "redirect:/groupList";
    }
    @GetMapping("/groupList/gt/{groupId}")
    public String gtGroup(@PathVariable("groupId") Long groupId, Model model) {
        model.addAttribute("allGroup", groupService.groupgtList(groupId));
        return "groupList";
    }
}
