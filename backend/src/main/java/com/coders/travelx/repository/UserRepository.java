package com.coders.travelx.repository;

import com.coders.travelx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String userEmail);
    boolean existsByEmail(String userEmail);
}