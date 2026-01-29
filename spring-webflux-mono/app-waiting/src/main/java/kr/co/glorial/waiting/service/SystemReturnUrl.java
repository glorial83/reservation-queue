package kr.co.glorial.waiting.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SystemReturnUrl {

    DEFAULT("default", "http://test.default.com:8090"),
    BOOKING("booking", "http://test.reservation.com:8090"),
    ;

    private final String siteName;
    private final String url;

    public static SystemReturnUrl fromSiteName(String siteName) {
        return java.util.Arrays.stream(SystemReturnUrl.values())
                .filter(v -> v.siteName.equalsIgnoreCase(siteName))
                .findFirst()
                .orElse(DEFAULT); // 못 찾을 경우 기본값 반환 (또는 예외 처리)
    }
}
