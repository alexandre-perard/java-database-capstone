package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Find an Admin by username.
     * Returns the Admin entity if found, otherwise null.
     */
    public Admin findByUsername(String username);

}
