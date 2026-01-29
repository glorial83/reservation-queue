package kr.co.glorial.waiting.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class VerifyEntryKeyRequest {
    private String userId;
    private String identifier;
}