package com.orabank.tfj.repository;

import com.orabank.tfj.model.HierarchicalLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HierarchicalLevelRepository extends JpaRepository<HierarchicalLevel, Long> {
    Optional<HierarchicalLevel> findByName(String name);
    List<HierarchicalLevel> findAllByOrderByLevelOrderAsc();
    boolean existsByName(String name);
}
