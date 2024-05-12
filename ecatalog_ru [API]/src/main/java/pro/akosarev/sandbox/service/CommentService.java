package pro.akosarev.sandbox.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.Comment;
import pro.akosarev.sandbox.repository.CommentRepository;

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

    public boolean saveComment(Comment comment, String userId, Long productId ) {

        comment.setUserId(userId);
        comment.setProductId(productId);
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
