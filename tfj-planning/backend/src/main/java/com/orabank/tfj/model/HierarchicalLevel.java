package com.orabank.tfj.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hierarchical_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HierarchicalLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 255)
    private String description;
    
    @Column(nullable = false)
    private Integer levelOrder;
    
    @OneToMany(mappedBy = "hierarchicalLevel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();
}
