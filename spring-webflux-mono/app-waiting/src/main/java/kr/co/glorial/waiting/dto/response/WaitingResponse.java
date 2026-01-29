package kr.co.glorial.waiting.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class WaitingResponse {

    // 식별자
    private String identifier;

    // 사용자 UUID
    private String userId;

    // 최초대기시간
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
