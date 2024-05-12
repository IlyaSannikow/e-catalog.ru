package pro.akosarev.sandbox.service;

import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.Path;
import pro.akosarev.sandbox.repository.PathRepository;

@Service
public class PathService {

    PathRepository pathRepository;

    public PathService(PathRepository pathRepository){
        this.pathRepository = pathRepository;
    }

    public String findPath(String name){
        return pathRepository.findByName(name).getPath();
    }

    public String findSource(String name) { return pathRepository.findByName(name).getSource(); }

    public Path findPathEntity(String name) { return pathRepository.findByName(name); }

}
