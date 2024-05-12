package pro.akosarev.sandbox.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.jboss.resteasy.plugins.server.servlet.Servlet3AsyncHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.akosarev.sandbox.entity.Comment;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.service.CommentService;
import pro.akosarev.sandbox.service.ProductService;

import java.security.Principal;
import java.util.List;

@Controller
public class CommentController {

    CommentService commentService;
    ProductService productService;

    public CommentController(CommentService commentService, ProductService productService){
        this.productService = productService;
        this.commentService = commentService;
    }

    @GetMapping("/product-info")
    public String createComment(@RequestParam(name = "name", defaultValue = "") String name,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request,
                                Principal principal,
                                Model model) {

        model.addAttribute("allComments", commentService.allComments());
        model.addAttribute("commentForm", new Comment());
        model.addAttribute("userId", principal.getName());

        HttpSession session = request.getSession();
        List<Product> products = (List<Product>) session.getAttribute("searchProduct");

        for (Product p : products) {
            if (p.getName().equals(name)) {
                model.addAttribute("product", p);
                break;
            }
        }

        return "product-info";
    }

    @PostMapping("/product-info")
    public String  deleteComment(@ModelAttribute("commentForm") @Valid Comment commentForm,
                                 @RequestParam(required = true, defaultValue = "" ) Long productId,
                                 @RequestParam(required = true, defaultValue = "" ) Long commentId,
                                 @RequestParam(required = true, defaultValue = "" ) String name,
                                 @RequestParam(required = true, defaultValue = "" ) String message,
                                 @RequestParam(required = true, defaultValue = "" ) String action,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal,
                                 Model model) {

        redirectAttributes.addAttribute("name", name);
        redirectAttributes.addAttribute("id", productId);
        String userid = principal.getName();

        if (action.equals("create")){
            if (bindingResult.hasErrors()) {
                return "product-info";
            }

            commentService.saveComment(commentForm, userid, productId);
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
        }

        return "redirect:/product-info";
    }
    @GetMapping("/product-info/gt/{commentId}")
    public String gtComment(@PathVariable("commentId") Long commentId, Model model) {
        model.addAttribute("allComments", commentService.commentgtList(commentId));
        return "product-info";
    }

}
