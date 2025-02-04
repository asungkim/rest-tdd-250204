package com.example.rest_tdd.domain.member.member.controller;

import com.example.rest_tdd.domain.member.member.service.MemberService;
import com.example.rest_tdd.global.dto.RsData;
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

    record JoinReqBody(String username, String password, String nickname) {}

    @PostMapping("/join")
    public RsData<Void> join(@RequestBody JoinReqBody body) {
        memberService.join(body.username(), body.password(), body.nickname());

        return new RsData<>("201-1", "회원가입이 완료되었습니다.");
    }
}
