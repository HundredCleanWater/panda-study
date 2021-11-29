package com.example.teampandanback.controller;

import com.example.teampandanback.dto.user.HeaderDto;
import com.example.teampandanback.dto.user.SignupRequestDto;
import com.example.teampandanback.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*
UserController를 대표하는 최상단 타이틀 영역에 표시 될 값을 세팅
Swagger 라이브러리 어노테이션
-REST API 개발 시 문서를 자동으로 만들어주는 프레임워크
-API에 대한 매뉴얼 자동 생성
-간단한 설정으로 프로젝트에 지정한 URL들을 HTML 화면으로 확인
 */
@Api(tags = {"로그인"})


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
@RestController
@RequiredArgsConstructor
public class UserController {

/*
private final을 선언하면 생성자를 통해 값을 참조 할 수 있다.
재할당하지 못하며, 해당 필드,메서드 별로 호출 할 때마다 새로이 값을 할당(인스턴스화)한다.
 */
    private final UserService userService;

    /*
    @ApiOperation : 메소드 설명, 해당 Controller안의 method의 설명이나 설정을 추가 할 수 있다.
    @GetMapping : Get Method 통신, user/kakao/callback과 매핑된다.
    -> user/kakao/callback에 매핑된 API에 대한 설명으로 "카카오 소셜 로그인"이 들어간다.
    메소드 구조 : public 리턴자료형 메소드명 (입력자료형1 입력변수 1 ){... return 리턴값}
    @RequestParam("가져 올 데이터 이름")[데이터타입][가져온 데이터를 담을 변수])
    RequestParm: 단일 HTTP요청 파라미터를 메소드 파라미터에 넣어줌
    code요청을 String타입으로 변환을 하고나면 code를 통해서 카카오로그인을 하고 , HeaderDto를 userService에 넘겨줌(아마 토큰을 넘길듯?)
     */
    @ApiOperation(value = "카카오 소셜 로그인")
    @GetMapping("/user/kakao/callback")
    public HeaderDto kakaoLogin(@RequestParam(value = "code") String code) {
        return userService.kakaoLogin(code);
    }

/*
    GET 통신에서는 RequestParam을 사용하지만 POST 통신에서는 RequestBody를 사용한다.
    Json(application/json) 형태의 HTTP Body 내용을 Java Object로 변환시켜주는 역할
    signupRequestDto에 있는 유저네임과 패스워드를 받아 입력 된 값을 registerUser로 넘겨준다.
    void타입이기 때문에 무언가를 반환하지 않아도 된다.

 */
    @ApiOperation(value = "기본 회원가입")
    @PostMapping("/user/signup")
    public void registerUser(@RequestBody SignupRequestDto requestDto) {
        userService.registerUser(requestDto);
    }

    @ApiOperation(value= "기본 로그인")
    @PostMapping("/user/login")
    public String login(@RequestBody SignupRequestDto requestDto) {
        return userService.login(requestDto);
    }
}
