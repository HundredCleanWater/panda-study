package com.example.teampandanback.domain.bookmark;

import com.example.teampandanback.dto.bookmark.response.BookmarkDetailForProjectListDto;
import com.example.teampandanback.dto.note.response.NoteEachBookmarkedResponseDto;
import com.example.teampandanback.dto.note.response.NoteEachSearchInBookmarkResponseDto;
import com.example.teampandanback.utils.CustomPageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepositoryQuerydsl {

    @Modifying(clearAutomatically = true)
    void deleteByProjectId(long projectId);

    Optional<Bookmark> findByUserIdAndNoteId(Long userId, Long noteId);

    CustomPageImpl<NoteEachBookmarkedResponseDto> findNoteByUserIdInBookmark(Long userId, Pageable pageable);

    List<NoteEachSearchInBookmarkResponseDto> findNotesByUserIdAndKeywordInBookmarks(Long userId, List<String> keywordList);

    @Modifying(clearAutomatically = true)
    void deleteByNote(Long noteId);

    // 유저가 가진 프로젝트들의 북마크 정보 조회
    List<BookmarkDetailForProjectListDto> findBookmarkCountByProject(List<Long> projectIdList, Long userId);

    // 해당 프로젝트내에서 유저가 북마크 했던 기록 삭제
    @Modifying(clearAutomatically = true)
    void deleteByProjectIdAndUserId(Long projectId, Long userId);

}
