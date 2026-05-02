package com.github.gwiman.mini_mes_backend.notification.domain;

/**
 * 알림 유형 enum.
 * <p>
 * 각 값은 메시지 템플릿을 보유하며, {@code formatMessage()}로 동적 값(문서번호 등)을 조합해
 * 최종 메시지를 생성한다. 새 알림 유형 추가 시 이 enum에 값과 템플릿만 추가하면 된다.
 * </p>
 */
public enum NotificationType {

    QUOTE_SUBMITTED    ("견적 %s 승인 요청이 도착했습니다."),
    QUOTE_APPROVED     ("견적 %s이 승인되었습니다."),
    QUOTE_REJECTED     ("견적 %s이 반려되었습니다."),
    PO_CREATED_FROM_PR ("구매 요청 %s이 발주로 전환되었습니다."),
    PO_CANCELLED       ("구매 발주 %s이 취소되었습니다."),
    GOODS_RECEIPT_CONFIRMED("자재 입고 %s이 확정되었습니다.");

    private final String messageTemplate;

    NotificationType(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /** 템플릿의 %s 자리에 동적 값(주로 문서번호)을 채워 최종 메시지를 반환한다. */
    public String formatMessage(String... args) {
        return String.format(messageTemplate, (Object[]) args);
    }
}
