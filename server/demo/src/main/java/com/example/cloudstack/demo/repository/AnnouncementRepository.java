package com.example.cloudstack.demo.repository;

import com.example.cloudstack.demo.model.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findTop5ByOrderByCreatedAtDesc();
}
