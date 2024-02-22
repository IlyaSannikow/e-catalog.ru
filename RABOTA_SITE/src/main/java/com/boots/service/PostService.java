package com.boots.service;

import com.boots.entity.Post;
import com.boots.repository.PostRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    @PersistenceContext
    private EntityManager em;
    PostRepository postRepository;
    public PostService(PostRepository postRepository){
        this.postRepository = postRepository;
    }

    public Post findPostById(Long postId) {
        Optional<Post> postFromDb = postRepository.findById(postId);
        return postFromDb.orElse(new Post());
    }

    public List<Post> allPost() {
        return postRepository.findAll();
    }

    public boolean savePost(@Valid Post post) {
        Post postFromDB = postRepository.findByName(post.getName());

        if (postFromDB != null) {
            return false;
        }

        postRepository.save(post);

        return true;
    }

    public boolean updatePost(Post post, String name, String message, String incidentDay, String outfit, String phoneNumber ) {

        post.setName(name);
	    post.setMessage(message);
	    post.setIncidentDay(incidentDay);
	    post.setOutfit(outfit);
	    post.setPhoneNumber(phoneNumber);
        postRepository.saveAndFlush(post);

        return true;
    }
    public boolean deletePost(Long postId) {
        if (postRepository.findById(postId).isPresent()) {
            postRepository.deleteById(postId);
            return true;
        }
        return false;
    }

    public List<Post> postgtList(Long idMin) {
        return em.createQuery("SELECT p FROM Post p WHERE p.id > :paramId", Post.class)
                .setParameter("paramId", idMin).getResultList();
    }
}
