package com.example.teampandanback.controller;

import com.example.teampandanback.OAuth2.UserDetailsImpl;
import com.example.teampandanback.dto.comment.request.CommentCreateRequestDto;
import com.example.teampandanback.dto.comment.request.CommentUpdateRequestDto;
import com.example.teampandanback.dto.comment.response.CommentCreateResponseDto;
import com.example.teampandanback.dto.comment.response.CommentUpdateResponseDto;
import com.example.teampandanback.dto.comment.response.CommentDeleteResponseDto;
import com.example.teampandanback.dto.comment.response.CommentReadListResponseDto;
import com.example.teampandanback.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
/*
UserController를 대표하는 최상단 타이틀 영역에 표시 될 값을 세팅
Swagger 라이브러리 어노테이션
-REST API 개발 시 문서를 자동으로 만들어주는 프레임워크
-API에 대한 매뉴얼 자동 생성
-간단한 설정으로 프로젝트에 지정한 URL들을 HTML 화면으로 확인
 */
@Api(tags = {"댓글"})

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

@RequiredArgsConstructor
@RestController
public class CommentController {
    /*
private final을 선언하면 생성자를 통해 값을 참조 할 수 있다.
재할당하지 못하며, 해당 필드,메서드 별로 호출 할 때마다 새로이 값을 할당(인스턴스화)한다.
 */
    private final CommentService commentService;
    /*
   @ApiOperation : 메소드 설명, 해당 Controller안의 method의 설명이나 설정을 추가 할 수 있다.
    @PathVariable : 경로의 특정 위치 값이 고정되지 않고 달라질 때 사용하는 것
    @AuthenticationPrincipal:Authentication(인증 정보에 대한 부분을 정의해놓은 인터페이스) + Principal("인증되는 주체의 ID")
    @RequestBody:객체 생성 가능하지만 각 변수별로 데이터 저장은 불가능(RequestParam과 반대)
    */
    //코멘트 작성
    @ApiOperation(value = "댓글 작성")
    @PostMapping("/api/comments/{noteId}")
    public CommentCreateResponseDto createComment(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CommentCreateRequestDto commentCreateRequestDto){
        return commentService.createComment(noteId,userDetails.getUser(),commentCreateRequestDto);
    }
    //코멘트 읽기
    @ApiOperation(value = "댓글 읽기")
    @GetMapping("/api/comments/{noteId}")
    public CommentReadListResponseDto readComments(@PathVariable Long noteId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return commentService.readComments(noteId,userDetails.getUser());
    }
    //코멘트 수정
    @ApiOperation(value = "댓글 수정")
    @PutMapping("/api/comments/{commentId}")
    public CommentUpdateResponseDto updateComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody CommentUpdateRequestDto commentUpdateRequestDto){
        return commentService.updateComment(commentId, userDetails.getUser(), commentUpdateRequestDto);
    }
    //코멘트 삭제
    @ApiOperation(value = "댓글 삭제")
    @DeleteMapping("/api/comments/{commentId}")
    public CommentDeleteResponseDto deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.deleteComment(commentId, userDetails.getUser());
    }
}
