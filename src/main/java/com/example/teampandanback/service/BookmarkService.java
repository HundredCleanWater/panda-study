package com.example.teampandanback.service;

import com.example.teampandanback.OAuth2.UserDetailsImpl;
import com.example.teampandanback.domain.bookmark.Bookmark;
import com.example.teampandanback.domain.bookmark.BookmarkRepository;
import com.example.teampandanback.domain.note.Note;
import com.example.teampandanback.domain.note.NoteRepository;
import com.example.teampandanback.domain.project.Project;
import com.example.teampandanback.domain.user.User;
import com.example.teampandanback.domain.user.UserRepository;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMapping;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMappingRepository;
import com.example.teampandanback.dto.note.response.NoteEachSearchInBookmarkResponseDto;
import com.example.teampandanback.dto.note.response.NoteSearchInBookmarkResponseDto;
import com.example.teampandanback.exception.ApiRequestException;
import com.example.teampandanback.utils.PandanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookmarkService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserProjectMappingRepository userProjectMappingRepository;
    private final PandanUtils pandanUtils;

    public void bookmarkNote(Long noteId, User currentUser) {

        //북마크 누른 사람
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );
        //북마크 될 노트
        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("생성되지 않은 노트입니다.")
        );

        //쿼리
                /*
              Optional -> Null인지 아닌지 확실 한 수 없는 객체를 담고 있을 경우 작성
        ofNulable은 null이 넘어 올 경우 Optional.empty()와 동일하게 비어 있는 Optional 객체를 얻어옴.
        해당객체가 null인지 아닌지 모르기때문에 empty대신 ofNullable을 사용함.
        즉, project가 있으면 그냥 꺼내면 되지만 만약 Null이면 비어있는 Optional 객체를 얻어와 연결된 프로젝트가 없다고 뜸
         */
        Project connectedProject = Optional.ofNullable(note.getProject()).orElseThrow(
                () -> new ApiRequestException("연결된 프로젝트가 없습니다.")
        );

        //쿼리

        UserProjectMapping userProjectMapping = userProjectMappingRepository.findByUserAndProject(user, connectedProject)
                .orElseThrow(
                        () -> new ApiRequestException("user와 project mapping을 찾지 못했습니다.")
                );

        //유저가 북마크를 했다는 레코드
          /*
        orElse(Value) -> Value가 메모리 상에 존재한다고 가정하므로 Value가 리턴값이라면 Optional 내부 값이 null이든 아니든 함수를 실행시켜 Value값을 가져 온다.
                        한마디로 객체 그대로 return한다.
        orElseGet -> Supplier 메소드를 받아서 return

        **Supplier : 함수적 인터페이스 API, 매개값은 없고 리턴값만으로 람다식을 사용 할 수 있다.
         */
        Bookmark bookmark = bookmarkRepository.findByUserAndNote(user, note)
                .orElseGet(() -> Bookmark.builder()
                        .user(user)
                        .note(note)
                        .build());

        bookmarkRepository.save(bookmark);
    }

    public void unBookmarkNote(Long noteId, User currentUser) {

        //북마크 누른 사람
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );

        //북마크 될 노트
        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("생성되지 않은 노트입니다.")
        );


        //쿼리
        Project connectedProject = Optional.ofNullable(note.getProject()).orElseThrow(
                () -> new ApiRequestException("연결된 프로젝트가 없습니다.")
        );

        //쿼리
        UserProjectMapping userProjectMapping = userProjectMappingRepository.findByUserAndProject(user, connectedProject)
                .orElseThrow(
                        () -> new ApiRequestException("user와 project mapping을 찾지 못했습니다.")
                );

        // 유저가 북마크를 했다는 레코드드
        /*
        ifPresent : null을 확인하는 if문을 줄이는데 사용
                   특정 결과를 반환하는 대신 Optional 객체가 감싸고 있는 값이 존재할 경우에만 실행될 로직을 함수형 인자로 넘길 수 있다.
                   null값 인경우 default값 반환
         */
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNote(user, note);

        bookmark.ifPresent(bookmarkRepository::delete);
        //= bookmark.ifPresent(bookmarkRepository.delete());
        //
    }

    public NoteSearchInBookmarkResponseDto searchNoteInBookmarks(User currentUser, String rawKeyword){
        List<String> keywordList = pandanUtils.parseKeywordToList(rawKeyword);
        List<NoteEachSearchInBookmarkResponseDto> resultList = bookmarkRepository.findNotesByUserIdAndKeywordInBookmarks(currentUser.getUserId(), keywordList);

        return NoteSearchInBookmarkResponseDto.builder().noteList(resultList).build();
    }
}
