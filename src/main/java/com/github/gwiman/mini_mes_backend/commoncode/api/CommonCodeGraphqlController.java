package com.github.gwiman.mini_mes_backend.commoncode.api;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeOptionDto;
import com.github.gwiman.mini_mes_backend.commoncode.application.CommonCodeService;

import lombok.RequiredArgsConstructor;

/**
 * 화면 초기화용 공통코드 GraphQL 컨트롤러.
 *
 * <p>클라이언트는 GraphQL alias를 이용해 me 쿼리와 함께 단일 요청으로 묶는다.
 * 기존 REST {@code POST /api/common-codes/search}는 공통코드 관리 화면의 CRUD 전용으로 유지된다.
 *
 * <p>설계 결정: docs/adr/0001-graphql-screen-init-with-common-codes.md
 */
@Controller
@RequiredArgsConstructor
public class CommonCodeGraphqlController {

    private final CommonCodeService commonCodeService;

    /**
     * 그룹코드에 속한 활성 공통코드를 정렬순서대로 반환한다.
     * CRUD용 {@link com.github.gwiman.mini_mes_backend.commoncode.api.dto.CommonCodeResponse}와 달리
     * 셀렉트 박스에 필요한 code·name만 포함한 경량 타입으로 응답한다.
     */
    @QueryMapping
    public List<CommonCodeOptionDto> commonCodes(@Argument String groupCode) {
        return commonCodeService.findByGroup(groupCode).stream()
            .map(r -> new CommonCodeOptionDto(r.code(), r.name()))
            .toList();
    }
}
