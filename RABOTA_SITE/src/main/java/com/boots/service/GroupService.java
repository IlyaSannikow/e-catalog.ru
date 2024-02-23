package com.boots.service;


import com.boots.entity.Group;
import com.boots.entity.Role;
import com.boots.entity.User;
import com.boots.repository.GroupRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
    @PersistenceContext
    private EntityManager em;
    GroupRepository groupRepository;
    public GroupService(GroupRepository groupRepository){
        this.groupRepository = groupRepository;
    }

    public Group findGroupById(Long groupId) {
        Optional<Group> groupFromDb = groupRepository.findById(groupId);
        return groupFromDb.orElse(new Group());
    }

    public List<Group> allGroup() {
        return groupRepository.findAll();
    }

    public boolean saveGroup(Group group) {
        Group groupFromDB = groupRepository.findByName(group.getName());

        if (groupFromDB != null) {
            return false;
        }

        groupRepository.save(group);

        return true;
    }

    public boolean addGroupUsers(Group group, User user){

        group.getUsers().add(user);
        groupRepository.save(group);

        return true;
    }

    public boolean removeGroupUsers(Group group, User user){

        group.getUsers().remove(user);
        groupRepository.save(group);

        return true;
    }

    public boolean updateGroup(Group group, String name) {

        group.setName(name);
        groupRepository.saveAndFlush(group);

        return true;
    }
    public boolean deleteGroup(Long groupId) {
        if (groupRepository.findById(groupId).isPresent()) {
            groupRepository.deleteById(groupId);
            return true;
        }
        return false;
    }

    public List<Group> groupgtList(Long idMin) {
        return em.createQuery("SELECT g FROM Group g WHERE g.id > :paramId", Group.class)
                .setParameter("paramId", idMin).getResultList();
    }
}
