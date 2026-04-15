package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.BookmarkCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeBookmarkCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 북마크 Command 컨트롤러
 * - POST /api/kms/bookmarks : 북마크 추가
 * - DELETE /api/kms/bookmarks : 북마크 취소
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/bookmarks")
@PreAuthorize("hasAnyAuthority('ADMIN', 'DL', 'TL', 'WORKER')")
public class WorkerBookmarkController {

    private final KnowledgeBookmarkCommandService bookmarkCommandService;

    // 북마크 추가 — body: { articleId }
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @AuthenticationPrincipal EmployeeUserDetails userDetails,
            @Valid @RequestBody BookmarkCreateRequest request) {
        bookmarkCommandService.addBookmark(
                request.getArticleId(),
                userDetails.getEmployeeId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("북마크가 추가되었습니다.", null));
    }

    // 북마크 취소 — param: articleId
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @RequestParam Long articleId,
            @AuthenticationPrincipal EmployeeUserDetails userDetails) {
        bookmarkCommandService.removeBookmark(articleId, userDetails.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success("북마크가 해제되었습니다.", null));
    }
}
