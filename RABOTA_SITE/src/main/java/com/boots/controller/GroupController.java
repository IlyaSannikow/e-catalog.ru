package com.boots.controller;

import com.boots.entity.Group;
import com.boots.service.GroupService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class GroupController {

    GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    @GetMapping("/createGroup")
    public String registration(Model model) {
        model.addAttribute("groupForm", new Group()); // Ключ, Значение

        return "createGroup";
    }

    @GetMapping("/groupList")
    public String groupList(Model model) {
        model.addAttribute("allGroup", groupService.allGroup()); // Ключ, Значение
        return "groupList";
    }

    @PostMapping("/createGroup")
    public String addGroup(@ModelAttribute("groupForm") @Valid Group groupForm, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "createGroup";
        }
        if (!groupService.saveGroup(groupForm)){
            model.addAttribute("groupNameError", "Группа с таким именем уже зарегестрирована");
            return "createGroup";
        }

        return "redirect:/";
    }

    @PostMapping("/groupList")
    public String  deleteProduct(@RequestParam(required = true, defaultValue = "" ) Long groupId,
                                 @RequestParam(required = true, defaultValue = "" ) String name,
                                 @RequestParam(required = true, defaultValue = "" ) String action,
                                 Model model) {

        if (action.equals("delete")){
            groupService.deleteGroup(groupId);
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
