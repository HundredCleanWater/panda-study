package com.example.teampandanback.service;

import com.example.teampandanback.OAuth2.Kakao.KakaoOAuth2;
import com.example.teampandanback.OAuth2.Kakao.KakaoUserInfo;
import com.example.teampandanback.OAuth2.UserDetailsImpl;
import com.example.teampandanback.domain.file.File;
import com.example.teampandanback.domain.note.Step;
import com.example.teampandanback.domain.user.User;
import com.example.teampandanback.domain.user.UserRepository;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMapping;
import com.example.teampandanback.domain.user_project_mapping.UserProjectMappingRepository;
import com.example.teampandanback.dto.file.request.FileDetailRequestDto;
import com.example.teampandanback.dto.note.request.NoteCreateRequestDto;
import com.example.teampandanback.dto.note.response.NoteCreateResponseDto;
import com.example.teampandanback.dto.project.request.ProjectRequestDto;
import com.example.teampandanback.dto.user.HeaderDto;
import com.example.teampandanback.dto.user.SignupRequestDto;
import com.example.teampandanback.exception.ApiRequestException;
import com.example.teampandanback.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
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
public class UserService {
    /*
private final을 선언하면 생성자를 통해 값을 참조 할 수 있다.
재할당하지 못하며, 해당 필드,메서드 별로 호출 할 때마다 새로이 값을 할당(인스턴스화)한다.
     */
    private final KakaoOAuth2 kakaoOAuth2;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserProjectMappingRepository userProjectMappingRepository;
    private final ProjectService projectService;
    private final NoteService noteService;

    /*
   @Value에서 값을 주입 시 $는 Properties의 값을 읽을 때 사용하고
   #은 Spring Expression Language을 사용 할 수 있다.
    Vlaue : Spring환경에서 Properties 정보를 가져오는 방법
     */
    @Value("${app.auth.tokenSecret}")
    private String secretKey;


    public HeaderDto kakaoLogin(String authorizedCode) {

        /*
        kakaOAuth2에있는 authorizedcode를 userInfo에 넣어준다.
         */
        KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(authorizedCode);

        /*
         userInfo에 있는 Id,이름,비밀번호를 하나씩 부른다. 비밀번호는 시크릿키를 이용해 암호화를 한다.
         passwordEncoder 인터페이스는 패스워드를 단방향으로 변환하여 패스워드를 안전하게 저장할 수 있게 해준다.
         인증 시 사용자가 제공한 암호와 비교해야하는 암호를 저장하는데 사용
         */
        Long kakaoId = userInfo.getId();
        String name = userInfo.getName();
        String password = passwordEncoder.encode(kakaoId + secretKey);


        String picture = userInfo.getPicture();
        String email = userInfo.getEmail();


        /*
        orElse는 해당 값이 null 이거나 null이 아니어도 실행된다.
        kakaoUser != null : kakaoUser가 있다면
        이름과 사진을 갱신해주고 userRepo에 저장해달라. 만약
        kakoUser에 없다면 밑에있는것들의 정보를 전부 kakaoUser에 넣고 레포에 저장
        빌더:가독성,유연성이 좋고 필요한 데이터만 설정가능.
         */
        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);
        if (kakaoUser != null) {
            kakaoUser.update(name, picture);
            userRepository.save(kakaoUser);
        } else {
            kakaoUser = User.builder()
                    .email(email)
                    .picture(picture)
                    .name(name)
                    .kakaoId(kakaoId)
                    .password(password)
                    .build();
            userRepository.save(kakaoUser);




        }
        /*
         위에서 저장된 kakaoUser를 새로운 userDetails로 만든다.
        SecurityContextHolder:Authentication를 담고 있는 Holder
        Authentication는 자체가 인증된 정보이기 때문에 SecurityContextHolder가 갖고 있는 값을 통해 인증이 되었는지 아닌지 확인을 할 수 있다.
        kakaoUser의 정보들을 문자열로 변환한 후
         */
        UserDetailsImpl userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HeaderDto headerDto = new HeaderDto();
        headerDto.setTOKEN(jwtTokenProvider.createToken(Long.toString(kakaoUser.getUserId()), email, name, picture));
        return headerDto;
    }
/*
일련의 작업들을 하나의 단위로 처리하기 위해 Transactional을 사(매서드가 포함하고 있는 작업 중 하나라도 실패 할 경우 전체 작업을 취소)
유저레포에서 아이디를 찾아 똑같은 아이디가 있으면 아이디가 중복됩니다 라고 띄우기
 */
    @Transactional
    public void registerUser(SignupRequestDto requestDto) {
        String name = requestDto.getUsername();
        User sameNameUser = userRepository.findByName(name).orElse(null);
        if (sameNameUser != null) {
            throw new ApiRequestException("아이디가 중복됩니다.");
        }
        String password;
        password = passwordEncoder.encode(requestDto.getPassword());
        User user = User.builder()
                .name(name)
                .password(password)
                //TODO: 바꿔야함
                .picture("https://s3.ap-northeast-2.amazonaws.com/front.blossomwhale.shop/ico-user.svg")
                .build();
        userRepository.save(user);
    }
    /*
    orElseThrow : 빈 값을 처리하기위한 접근법
    찾은 네임이 없다?? 그럼 가입이 안되어 있으니 가입되지 않은 유저!
    그리고 만약 패스워드가 맞지않으면(매치되지 않으면) 잘못된 비밀번호라고 말해줌
     */
    @Transactional
    public String login(SignupRequestDto requestDto) {
        User user = userRepository.findByName(requestDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 유저입니다."));
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }
        return jwtTokenProvider.createToken(Long.toString(user.getUserId()), "", user.getName(), "");
    }


    @Transactional
    public Long getLastUserId() {
        User lastUser = userRepository.getLastUser().orElseThrow(
                () -> new ApiRequestException("유저가 하나도 없습니다.")
        );

        return lastUser.getUserId();
    }


    @Transactional
    public Long getCountOfUserInvitedToProject(Long userId) {
        return userProjectMappingRepository.getCountOfUserInvitedToProject(userId);
    }

    /*
    이부분은 좀 어려운듯..? 매핑레포에 쿼리있고 뭐가 많음..
    유저프로젝트매핑레포에서 프로젝트와 유저아이디를 찾아서 있으면 optuser에 넣어줘야하는데 null이 오면 optional로 감싼다.
    만약 null이 아니라면 true null이면 false
     */
    @Transactional
    public Boolean isUserInvitedToProject(Long userId, Long projectId) {
        Optional<UserProjectMapping> optUserProjectMapping = userProjectMappingRepository.findByUserIdAndProjectId(userId, projectId);

        if (optUserProjectMapping.isPresent()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
