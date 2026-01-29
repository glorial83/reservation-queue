package kr.co.glorial.waiting.dto.request;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class AppendUserRequest {
    private String systemName;
    private String returnUrl;

    @Setter(AccessLevel.NONE)
    private final long timestamp = Instant.now().toEpochMilli();
}