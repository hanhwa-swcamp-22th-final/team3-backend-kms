package com.ohgiraffers.team3backendkms.jwt;

public final class AuthenticatedEmployee {

    private AuthenticatedEmployee() {
    }

    public static Long employeeId(EmployeeUserDetails userDetails, Long fallbackEmployeeId) {
        return userDetails != null ? userDetails.getEmployeeId() : fallbackEmployeeId;
    }

    public static String role(EmployeeUserDetails userDetails, String fallbackRole) {
        if (userDetails == null || userDetails.getAuthorities().isEmpty()) {
            return normalizeRole(fallbackRole);
        }
        return normalizeRole(userDetails.getAuthorities().iterator().next().getAuthority());
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return role;
        }

        String normalized = role.trim().toUpperCase();
        // JWT claim/authority 는 ROLE_* 형태로 올 수 있어 KMS 권한 분기 전에 표준화한다.
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return switch (normalized) {
            case "TL", "TEAMLEADER" -> "TEAMLEADER";
            case "DL", "DEPARTMENTLEADER" -> "DEPARTMENTLEADER";
            case "WORKER" -> "WORKER";
            case "ADMIN" -> "ADMIN";
            case "HRM" -> "HRM";
            default -> normalized;
        };
    }
}
