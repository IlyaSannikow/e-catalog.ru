package com.boots.controller;

import com.boots.entity.Comment;
import com.boots.entity.User;
import com.boots.service.CommentService;
import com.boots.service.ProductService;
import com.boots.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.Id;
import javax.validation.Valid;

@Controller
public class CommentController {

    CommentService commentService;
    ProductService productService;

    UserService userService;

    public CommentController(CommentService commentService, ProductService productService, UserService userService){
        this.productService = productService;
        this.commentService = commentService;
        this.userService = userService;
    }

    @GetMapping("/product-info")
    public String createProduct(@RequestParam(name = "id", defaultValue = "") Long id,
                                Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute("allComments", commentService.allComments());
        model.addAttribute("commentForm", new Comment()); // Ключ, Значение

        model.addAttribute("product", productService.findProductById(id));
        model.addAttribute("user", userService.findUserByUsername(authentication.getName()));
        return "product-info";
    }
    @PostMapping("/product-info")
    public String  deleteComment(@ModelAttribute("commentForm") @Valid Comment commentForm,
                                 @RequestParam(required = true, defaultValue = "" ) Long productId,
                                 @RequestParam(required = true, defaultValue = "" ) Long commentId,
                                 @RequestParam(required = true, defaultValue = "" ) String message,
                                 @RequestParam(required = true, defaultValue = "" ) String action,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        redirectAttributes.addAttribute("id", productId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        if (action.equals("create")){
            if (bindingResult.hasErrors()) {
                return "product-info";
            }

            User userFromBD = userService.findUserByUsername(currentPrincipalName);
            commentService.saveComment(commentForm, userFromBD.getId());
        }

        if (action.equals("delete")){
            commentService.deleteComment(commentId);
        }
        if (action.equals("update")){
            Comment comment = commentService.findCommentById(commentId);
            model.addAttribute("comment", comment);
            model.addAttribute("product", productService.findProductById(productId));
            return "/comment-update";
        }
        if (action.equals("update2")){
            Comment comment = commentService.findCommentById(commentId);
            commentService.updateComment(comment, message);
            model.addAttribute("product", productService.findProductById(productId));
        }

        return "redirect:/product-info";
    }
    @GetMapping("/product-info/gt/{commentId}")
    public String gtComment(@PathVariable("commentId") Long commentId, Model model) {
        model.addAttribute("allComments", commentService.commentgtList(commentId));
        return "product-info";
    }

}
