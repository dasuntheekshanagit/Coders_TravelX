package com.coders.travelx.repository;

import com.coders.travelx.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode,Integer> {
    VerificationCode findByCode(String code);
}
