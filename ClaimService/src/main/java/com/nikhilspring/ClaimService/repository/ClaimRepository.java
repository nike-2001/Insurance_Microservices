package com.nikhilspring.ClaimService.repository;

import com.nikhilspring.ClaimService.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Claim findFirstByPolicyIdOrderByClaimDateDesc(long policyId);
} 