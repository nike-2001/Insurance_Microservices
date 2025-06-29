package com.nikhilspring.ClaimService.service;

import com.nikhilspring.ClaimService.model.ClaimRequest;
import com.nikhilspring.ClaimService.model.ClaimResponse;

public interface ClaimService {
    long fileClaim(ClaimRequest claimRequest);
    ClaimResponse getClaimByPolicyId(long policyId);
    ClaimResponse getClaimById(long claimId);
} 