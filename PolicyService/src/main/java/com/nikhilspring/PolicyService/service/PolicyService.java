package com.nikhilspring.PolicyService.service;

import com.nikhilspring.PolicyService.model.PolicyRequest;
import com.nikhilspring.PolicyService.model.PolicyResponse;

public interface PolicyService {
    long issuePolicy(PolicyRequest policyRequest);

    PolicyResponse getPolicyDetails(long policyId);
}