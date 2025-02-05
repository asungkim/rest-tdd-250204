package com.example.rest_tdd.global;

import com.example.rest_tdd.domain.member.member.entity.Member;
import com.example.rest_tdd.domain.member.member.service.MemberService;
import com.example.rest_tdd.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

// Request, Response, Session, Cookie, Header
@Component
@RequiredArgsConstructor
@RequestScope // request 마다 주입
public class Rq {

    private final HttpServletRequest request;
    private final MemberService memberService;

    public Member getAuthenticatedWriter() {
        String authorizationValue = request.getHeader("Authorization");

        String apiKey = authorizationValue.replaceAll("Bearer ","");
        Optional<Member> opWriter = memberService.findByApiKey(apiKey);

        if (opWriter.isEmpty()) {
            throw new ServiceException("401-1", "잘못된 인증키입니다.");
        }

        return opWriter.get();
    }
}
