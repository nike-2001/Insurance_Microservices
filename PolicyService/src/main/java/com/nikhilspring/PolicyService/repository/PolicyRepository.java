package com.nikhilspring.PolicyService.repository;

import com.nikhilspring.PolicyService.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy,Long> {
}
