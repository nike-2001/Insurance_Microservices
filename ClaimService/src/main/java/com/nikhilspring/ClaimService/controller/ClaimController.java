package com.nikhilspring.ClaimService.controller;

import com.nikhilspring.ClaimService.model.ClaimRequest;
import com.nikhilspring.ClaimService.model.ClaimResponse;
import com.nikhilspring.ClaimService.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/claim")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping
    public ResponseEntity<Long> fileClaim(@RequestBody ClaimRequest claimRequest) {
        return new ResponseEntity<>(
                claimService.fileClaim(claimRequest),
                HttpStatus.OK
        );
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<ClaimResponse> getClaimByPolicyId(@PathVariable long policyId) {
        return new ResponseEntity<>(
                claimService.getClaimByPolicyId(policyId),
                HttpStatus.OK
        );
    }

    @GetMapping("/{claimId}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable long claimId) {
        return new ResponseEntity<>(
                claimService.getClaimById(claimId),
                HttpStatus.OK
        );
    }
} 