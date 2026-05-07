package com.edusys.backend.service;

import com.edusys.backend.model.Announcement;
import com.edusys.backend.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repo;

    public AnnouncementService(AnnouncementRepository repo) {
        this.repo = repo;
    }

    public Announcement save(Announcement a) {
        return repo.save(a);
    }

    public Optional<Announcement> findById(Long id) {
        return repo.findById(id);
    }

    public List<Announcement> findAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
