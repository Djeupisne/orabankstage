package com.orabank.tfj.repository;

import com.orabank.tfj.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    Optional<Service> findByName(String name);
    boolean existsByName(String name);
}
