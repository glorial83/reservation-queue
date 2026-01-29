package kr.co.glorial.waiting.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class VerifyEntryKeyResponse {
    // 입장허가
    private boolean allowed;

    // 리턴URL
    private String returnUrl;
}
