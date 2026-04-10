package com.ohgiraffers.team3backendkms.kms.command.application.controller.worker;

import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import com.ohgiraffers.team3backendkms.kms.command.application.dto.request.BookmarkCreateRequest;
import com.ohgiraffers.team3backendkms.kms.command.application.service.KnowledgeBookmarkCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 북마크 Command 컨트롤러
 * - POST /api/kms/bookmarks : 북마크 추가
 * - DELETE /api/kms/bookmarks : 북마크 취소
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kms/bookmarks")
public class WorkerBookmarkController {

    private final KnowledgeBookmarkCommandService bookmarkCommandService;

    // 북마크 추가 — body: { articleId, employeeId }
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addBookmark(
            @Valid @RequestBody BookmarkCreateRequest request) {
        bookmarkCommandService.addBookmark(request.getArticleId(), request.getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("북마크가 추가되었습니다.", null));
    }

    // 북마크 취소 — param: articleId, employeeId
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @RequestParam Long articleId,
            @RequestParam Long employeeId) {
        bookmarkCommandService.removeBookmark(articleId, employeeId);
        return ResponseEntity.ok(ApiResponse.success("북마크가 해제되었습니다.", null));
    }
}
