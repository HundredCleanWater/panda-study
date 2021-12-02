package com.example.teampandanback.controller;

import com.example.teampandanback.OAuth2.UserDetailsImpl;
import com.example.teampandanback.dto.note.response.NoteSearchInBookmarkResponseDto;
import com.example.teampandanback.service.BookmarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"북마크"})
@RequiredArgsConstructor
@RestController
/*
@RestController
Json형태로 객체 데이터를 반환/ 메소드마다 ResponseBody를 입력 해 줄 필요가 없음
1.Client는 URL형식으로 웹 서비스에 요청
2.Mapping되는 Handler와 그 Type을 찾는 DispatcherServlet이 요청을 인터셉트
3.RestController는 해당 요청을 처리하고 데이터를 반환

@RequiredArgsConstructor
초기화 되지 않은 final 필드나, @NonNull이 붙은 필드에 대해 생성자를 생성
의존성 주입 편의성을 위해서 사용
 */
public class BookmarkController {

    private final BookmarkService bookmarkService;

    //북마크 함
    @ApiOperation(value = "북마크 하기")
    @PostMapping("/api/notes/{noteId}/bookmark")
    public void bookmarkNote(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        bookmarkService.bookmarkNote(noteId,userDetails.getUser());
    }

    //북마크 해제
    @ApiOperation(value = "북마크 해제")
    @PostMapping("/api/notes/{noteId}/unbookmark")
    public void unBookmarkNote(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        bookmarkService.unBookmarkNote(noteId,userDetails.getUser());
    }

    // 북마크한 노트들 중에서 노트 제목 검색
    @ApiOperation(value = "내가 북마크 한 노트들 중에서 노트 검색 (제목으로)", notes = "http://{hostName}/api/notes/search/bookmarks/?keyword=xxxx")
    @GetMapping("/api/notes/search/bookmarks")
    public NoteSearchInBookmarkResponseDto searchNoteInBookmark(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam("keyword") String rawKeyword){
        return bookmarkService.searchNoteInBookmarks(userDetails.getUser(), rawKeyword);
    }
}
