package com.example.teampandanback.controller;

import com.example.teampandanback.dto.user.HeaderDto;
import com.example.teampandanback.dto.user.SignupRequestDto;
import com.example.teampandanback.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@Api(tags = {"로그인"})
// Swagger 라이브러리 어노테이션
// Swagger란?
// - 문서 자동화 툴은 간단한 설정만으로도 테스트 가능한 Web UI를 지원하여 API 를 테스트를 위해
//   부가적으로 서드파티 프로그램을 깔 필요가 없다.
// - 최소한의 작업을 통해 자동으로 API Document 를 만들어주므로
//   클라이언트 개발자에게 문서 내용을 전달하기 위해 추가 작업을 하지 않아도 된다.
// Swagger API 문서 자동화 방법 => https://daddyprogrammer.org/post/313/swagger-api-doc/


@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation(value = "카카오 소셜 로그인")
    @GetMapping("/user/kakao/callback")
    public HeaderDto kakaoLogin(@RequestParam(value = "code") String code) {
        return userService.kakaoLogin(code);
    }

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
