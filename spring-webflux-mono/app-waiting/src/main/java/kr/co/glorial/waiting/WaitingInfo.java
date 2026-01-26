package kr.co.glorial.waiting;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
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
}
