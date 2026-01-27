package kr.co.glorial.booking.service;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class WaitingInfo {

    // 식별자
    private String identifier;

    // 사용자 UUID
    private String userId;

    // 입장시간
    private long timestamp;

    // 대기순번
    private long position;

    // 전체
    private long total;

    // 입장허가
    private boolean allowed;

    // 리턴URL
    private String returnUrl;
}
