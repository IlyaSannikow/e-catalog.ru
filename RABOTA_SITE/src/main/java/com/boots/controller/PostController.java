package com.boots.controller;

import com.boots.entity.Post;
import com.boots.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class PostController {

    PostService postService;

    public PostController(PostService postService){
        this.postService = postService;
    }

    @GetMapping("/postList")
    public String postList(Model model) {
        model.addAttribute("allPost", postService.allPost()); // Ключ, Значение
        model.addAttribute("postForm", new Post());
        return "postList";
    }

    @PostMapping("/postList")
    public String  deleteProduct(@ModelAttribute("postForm") @Valid Post postForm,
                                 @RequestParam(required = true, defaultValue = "" ) Long postId,
                                 @RequestParam(required = true, defaultValue = "" ) String name,
				                 @RequestParam(required = true, defaultValue = "" ) String message,
				                 @RequestParam(required = true, defaultValue = "" ) String incidentDay,
				                 @RequestParam(required = true, defaultValue = "" ) String outfit,
				                 @RequestParam(required = true, defaultValue = "" ) String phoneNumber,
                                 @RequestParam(required = true, defaultValue = "" ) String action,
                                 BindingResult bindingResult,
                                 Model model) {
        if (action.equals("create")){
            if (bindingResult.hasErrors()) {
                return "postList";
            }
            postService.savePost(postForm);

            return "redirect:/postList";
        }
        if (action.equals("delete")){
            postService.deletePost(postId);
        }
        if (action.equals("update")){
            Post post = postService.findPostById(postId);
            model.addAttribute("post", post);
            return "/post-update";
        }
        if (action.equals("update2")){
            Post post = postService.findPostById(postId);
            postService.updatePost(post, name, message, incidentDay, outfit, phoneNumber);
        }

        return "redirect:/postList";
    }
    @GetMapping("/postList/gt/{postId}")
    public String gtPost(@PathVariable("postId") Long postId, Model model) {
        model.addAttribute("allPost", postService.postgtList(postId));
        return "postList";
    }

}
