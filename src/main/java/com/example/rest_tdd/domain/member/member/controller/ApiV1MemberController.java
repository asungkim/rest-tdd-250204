package com.example.rest_tdd.domain.member.member.controller;

import com.example.rest_tdd.domain.member.member.dto.MemberDto;
import com.example.rest_tdd.domain.member.member.service.MemberService;
import com.example.rest_tdd.global.Rq;
import com.example.rest_tdd.global.dto.RsData;
import com.example.rest_tdd.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;
    private final Rq rq;

    record JoinReqBody(String username, String password, String nickname) {
    }

    @PostMapping("/join")
    public RsData<MemberDto> join(@RequestBody JoinReqBody body) {

        memberService.findByUsername(body.username())
                .ifPresent(_ -> {
                            throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                        }
                );

        com.example.rest_tdd.domain.member.member.entity.Member member = memberService.join(body.username(), body.password(), body.nickname());
        return new RsData<>(
                "201-1",
                "회원가입이 완료되었습니다.",
                new MemberDto(member));
    }

    record LoginReqBody(String username, String password) {
    }

    record LoginResBody(MemberDto item, String apiKey) {
    }

    @PostMapping("/login")
    public RsData<LoginResBody> login(@RequestBody LoginReqBody body) {

        com.example.rest_tdd.domain.member.member.entity.Member member = memberService.findByUsername(body.username()).get();

        if (!member.getPassword().equals(body.password())) {
            throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");
        }

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getNickname()),
                new LoginResBody(new MemberDto(member), member.getApiKey())
        );
    }


}
