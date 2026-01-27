package kr.co.glorial.waiting.controller;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class VerifyUserDTO {
    private String userId;
    private String systemName;
}