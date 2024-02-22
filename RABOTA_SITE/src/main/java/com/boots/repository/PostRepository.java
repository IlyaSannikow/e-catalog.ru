package com.boots.repository;

import com.boots.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> { // <Сущность, тип id>
    Post findByName(String name);
    Optional<Post> findById(Long id);

}
