package com.medical.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public void assertPatientAccess(Integer patientId) {
        UserPrincipal principal = getCurrentPrincipal();
        if (principal == null) {
            throw new AccessDeniedException("Unauthorized");
        }

        boolean isPatient = principal.getRoles().contains("PATIENT");
        if (!isPatient) {
            return;
        }

        if (principal.getPatientId() == null || !principal.getPatientId().equals(patientId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public Integer getCurrentUserId() {
        UserPrincipal principal = getCurrentPrincipal();
        return principal == null ? null : principal.getUserId();
    }

    private UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return (UserPrincipal) authentication.getPrincipal();
    }
}
