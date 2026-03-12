package com.github.gwiman.mini_mes_backend.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 문서 번호 자동 채번 컴포넌트.
 * <p>
 * 채번 규칙: {@code <PREFIX>_yyyyMM_###} (예: {@code QT_202603_001}, {@code SO_202603_001})
 * 동일 prefix + 월 내에서 MAX 조회 후 순번 +1 방식으로 생성한다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DocumentNumberGenerator {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile(".*_(\\d+)$");

    private final DSLContext dsl;

    /**
     * 문서 번호를 생성한다.
     *
     * @param prefix      문서 유형 접두사 (예: {@code "QT_"}, {@code "SO_"})
     * @param numberField 번호가 저장된 jOOQ 테이블 필드 (MAX 조회 및 테이블 추론에 사용)
     * @return 생성된 문서 번호 (예: {@code QT_202603_001})
     */
    public String generate(String prefix, TableField<?, String> numberField) {
        String fullPrefix = prefix + LocalDate.now().format(MONTH_FORMAT) + "_";
        String prefixPattern = fullPrefix + "%";

        int maxSeq = dsl
            .select(DSL.max(numberField))
            .from(numberField.getTable())
            .where(numberField.like(prefixPattern))
            .fetchOptional()
            .flatMap(r -> Optional.ofNullable(r.value1()))
            .map(s -> {
                var m = SEQUENCE_PATTERN.matcher(s);
                return m.find() ? Integer.parseInt(m.group(1)) : 0;
            })
            .orElse(0);

        return fullPrefix + String.format("%03d", maxSeq + 1);
    }
}
