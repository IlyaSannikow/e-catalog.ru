package com.boots.repository;

import com.boots.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> { // <Сущность, тип id>
    Group findByName(String name);
    Optional<Group> findById(Long id);

}
