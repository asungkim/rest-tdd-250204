package com.example.rest_tdd.domain.member.member.controller;

import com.example.rest_tdd.domain.member.member.dto.MemberDto;
import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.member.member.service.MemberService;
import com.example.rest_tdd.global.dto.RsData;
import com.example.rest_tdd.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {

    private final MemberService memberService;

    record JoinReqBody(String username, String password, String nickname) {
    }

    @PostMapping("/join")
    public RsData<MemberDto> join(@RequestBody JoinReqBody body) {

        memberService.findByUsername(body.username())
                .ifPresent(_ -> {
                            throw new ServiceException("409-1", "이미 사용중인 아이디입니다.");
                        }
                );

        Member member = memberService.join(body.username(), body.password(), body.nickname());
        return new RsData<>(
                "201-1",
                "회원가입이 완료되었습니다.",
                new MemberDto(member));
    }

    record LoginReqBody(String username, String password) {
    }


    @PostMapping("/login")
    public RsData<MemberDto> login(@RequestBody LoginReqBody body) {

        Member member = memberService.findByUsername(body.username()).get();

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getNickname()),
                new MemberDto(member)
        );
    }

}
