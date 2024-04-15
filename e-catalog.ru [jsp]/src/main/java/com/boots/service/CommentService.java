package com.boots.service;

import com.boots.entity.Comment;
import com.boots.repository.CommentRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService  {
    @PersistenceContext
    private EntityManager em;
    CommentRepository commentRepository;
    public CommentService(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }
    public Comment findCommentById(Long commentId) {
        Optional<Comment> commentFromDb = commentRepository.findById(commentId);
        return commentFromDb.orElse(new Comment());
    }

    public List<Comment> allComments() {
        return commentRepository.findAll();
    }

    public boolean saveComment(Comment comment, Long userId) {

        comment.setUserId(userId);
        commentRepository.save(comment);

        return true;
    }

    public boolean updateComment(Comment comment, String message) {

        comment.setMessage(message);

        commentRepository.saveAndFlush(comment);

        return true;
    }
    public boolean deleteComment(Long commentId) {
        if (commentRepository.findById(commentId).isPresent()) {
            commentRepository.deleteById(commentId);
            return true;
        }
        return false;
    }

    public List<Comment> commentgtList(Long idMin) {
        return em.createQuery("SELECT u FROM Comment u WHERE u.id > :paramId", Comment.class)
                .setParameter("paramId", idMin).getResultList();
    }
}


