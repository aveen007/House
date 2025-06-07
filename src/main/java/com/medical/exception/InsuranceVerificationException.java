package com.medical.exception;

public class InsuranceVerificationException extends RuntimeException {
    public InsuranceVerificationException(String message) {
        super(message);
    }
}