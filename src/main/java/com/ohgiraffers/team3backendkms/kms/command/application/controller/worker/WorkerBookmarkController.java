package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.config.security.SecurityContextUtils;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.BookmarkCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeBookmarkCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/bookmarks")
public class WorkerBookmarkController {

    private final KnowledgeBookmarkCommandService bookmarkCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            Authentication authentication,
            @Valid @RequestBody BookmarkCreateRequest request) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        bookmarkCommandService.addBookmark(request.getArticleId(), userDetails.getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("북마크가 추가되었습니다.", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            Authentication authentication,
            @RequestParam Long articleId) {
        EmployeeUserDetails userDetails = currentUser(authentication);
        bookmarkCommandService.removeBookmark(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("북마크가 해제되었습니다.", null));
    }

    private EmployeeUserDetails currentUser(Authentication authentication) {
        return SecurityContextUtils.currentUser(authentication);
    }
}
