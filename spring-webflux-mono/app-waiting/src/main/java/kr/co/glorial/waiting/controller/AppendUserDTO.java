package kr.co.glorial.waiting.controller;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class AppendUserDTO {
    private String userId;
    private String systemName;

    @Setter(AccessLevel.NONE)
    private final long timestamp = Instant.now().toEpochMilli();
}