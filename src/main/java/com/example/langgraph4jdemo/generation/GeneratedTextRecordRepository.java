package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.auth.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneratedTextRecordRepository extends JpaRepository<GeneratedTextRecord, Long> {

    Page<GeneratedTextRecord> findByUserOrderByCreatedAtDesc(AppUser user, Pageable pageable);

    Optional<GeneratedTextRecord> findByIdAndUser(Long id, AppUser user);
}
