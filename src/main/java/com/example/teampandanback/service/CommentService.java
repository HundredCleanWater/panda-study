package com.example.teampandanback.service;


import com.example.teampandanback.domain.Comment.Comment;
import com.example.teampandanback.domain.Comment.CommentRepository;
import com.example.teampandanback.domain.note.Note;
import com.example.teampandanback.domain.note.NoteRepository;
import com.example.teampandanback.domain.project.Project;
import com.example.teampandanback.domain.user.User;
import com.example.teampandanback.domain.user.UserRepository;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMapping;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMappingRepository;
import com.example.teampandanback.dto.comment.request.CommentCreateRequestDto;
import com.example.teampandanback.dto.comment.request.CommentUpdateRequestDto;
import com.example.teampandanback.dto.comment.response.CommentCreateResponseDto;
import com.example.teampandanback.dto.comment.response.CommentUpdateResponseDto;
import com.example.teampandanback.dto.comment.response.CommentDeleteResponseDto;
import com.example.teampandanback.dto.comment.response.CommentReadEachResponseDto;
import com.example.teampandanback.dto.comment.response.CommentReadListResponseDto;
import com.example.teampandanback.exception.ApiRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/*
@RequiredArgsConstructor
초기화 되지 않은 final 필드나, @NonNull이 붙은 필드에 대해 생성자를 생성
의존성 주입 편의성을 위해서 사용
@Service
 클래스를 스프링이 시작할 때 Bean으로 등록해 줌
 해당클래스가 Service클래스 임을 확실하게 증명해줌
 */
@RequiredArgsConstructor
@Service
public class CommentService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final CommentRepository commentRepository;
    private final UserProjectMappingRepository userProjectMappingRepository;


    /*
    orElseThrow -> 만약 값이 없으면 예외적으로 밑에 문구를 나타냄
     */
    public CommentCreateResponseDto createComment(Long noteId, User currentUser, CommentCreateRequestDto commentCreateRequestDto) {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );

        Note note = noteRepository.findById(noteId).orElseThrow(
                () -> new ApiRequestException("생성되지 않은 노트입니다.")
        );
        /*
        Optional -> Null인지 아닌지 확실 한 수 없는 객체를 담고 있을 경우 작성
        ofNullable은 null이 넘어 올 경우 Optional.empty()와 동일하게 비어 있는 Optional 객체를 얻어옴.
        해당객체가 null인지 아닌지 모르기때문에 empty대신 ofNullable을 사용함.
        즉, project가 있으면 그냥 꺼내면 되지만 만약 Null이면 비어있는 Optional 객체를 얻어와 연결된 프로젝트가 없다고 뜸
         */
        //쿼리
        Project connectedProject = Optional.ofNullable(note.getProject()).orElseThrow(
                () -> new ApiRequestException("연결된 프로젝트가 없습니다.")
        );

        /*
        User와 Project에서 user와 connedctedProject를 찾아주고 만약 없으면 문구 반환하기
         */
        //쿼리
        UserProjectMapping userProjectMapping = userProjectMappingRepository.findByUserAndProject(user, connectedProject)
                .orElseThrow(
                        () -> new ApiRequestException("user와 project mapping을 찾지 못했습니다.")
                );

        /*

         */
        Comment newComment = Comment.builder()
                .user(user)
                .note(note)
                .content(commentCreateRequestDto.getContent())
                .build();

        Comment savedComment = commentRepository.save(newComment);

        return CommentCreateResponseDto.builder()
                .content(savedComment.getContent())
                .commentId(savedComment.getCommentId())
                .writer(savedComment.getUser().getName())
                .build();
    }

    public CommentReadListResponseDto readComments(Long noteId, User currentUser) {
        User user = userRepository.findById(currentUser.getUserId()).orElseThrow(
                () -> new ApiRequestException("등록되지 않은 유저의 접근입니다.")
        );

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
        /*
        stream : 람다함수형식으로 배열들을 가공 ( for , foreach문을 사용안해도 됨.. 이거쓰는사람 부러움..)
        map: 요소들의 특정 조건에 해당하는 값으로 변환해줌
        collect : 가공이 끝난 요소들을 리턴해줄 결과값을 넣어 줌 .
        sorted : 요소들을 정렬-> getCreatedAt: 댓글생성 시간순으로 정렬하는듯 .
         */
        List<Comment> commentList = commentRepository.findByNoteId(noteId);
        List<Comment> commentListSortedByCreatedAt = commentList.stream().sorted(Comparator.comparing(Comment::getCreatedAt)).collect(Collectors.toList());
        List<CommentReadEachResponseDto> commentReadEachResponseDtoList =
                commentListSortedByCreatedAt
                        .stream()
                        .map(e -> CommentReadEachResponseDto.fromEntity(e))
                        .collect(Collectors.toList());


        return CommentReadListResponseDto.builder()
                .commentList(commentReadEachResponseDtoList)
                .build();
    }

    // 댓글 수정
    /*
    .filter : 특정조건으로 스트림의 컨텐츠를 필터링 하는 것.현재 로그인 되어 있는 Id와 댓글 작성한 Id가 같은지 필터링을 거치는 듯?
    .map : 어떻게 변경 시킬것인가? -> 코멘트업데이트리퀘스트디티오에 업데이트 한다!
    .orElseThrow : 아이디가 다르면 이제 저 문구을 보여줘
     */
    @Transactional
    public CommentUpdateResponseDto updateComment(Long commentId, User currentUser, CommentUpdateRequestDto commentUpdateRequestDto) {

        Optional<Comment> maybeComment = commentRepository.findById(commentId);

        Comment updateComment = maybeComment
                                    .filter(c->c.getUser().getUserId().equals(currentUser.getUserId()))
                                    .map(c->c.update(commentUpdateRequestDto))
                                    .orElseThrow(() -> new ApiRequestException("댓글은 본인만 수정할 수 있습니다"));

        return  CommentUpdateResponseDto.fromEntity(updateComment);
    }

    // 댓글 삭제
    @Transactional
    public CommentDeleteResponseDto deleteComment(Long commentId, User currentUser) {
        commentRepository.deleteByCommentIdAndUserId(commentId, currentUser.getUserId());

        return CommentDeleteResponseDto.builder()
                .commentId(commentId)
                .build();

    }
}
