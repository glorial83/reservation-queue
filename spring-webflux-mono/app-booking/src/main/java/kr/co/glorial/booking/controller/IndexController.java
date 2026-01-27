package kr.co.glorial.booking.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.glorial.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Controller
public class IndexController {

    @Value("${waiting.host}")
    private String waitingHost;

    @Value("${booking.host}")
    private String bookingHost;

    private String systemName = "booking";

    private final AuthService service;

    @GetMapping("/")
    public String index(
            @RequestParam(required = false, defaultValue = "none", name = "entryTicket") String entryTicket,
            HttpServletResponse response
    ) {
        // 토큰 검증 후 예약 페이지로 이동
        if (!"none".equals(entryTicket) && service.checkEntryTicket(entryTicket)) {
            return "booking";
        }

        // 대기 사이트로 이동
        log.info("입장 티켓이 없습니다.");

        return "redirect:%s/waiting?systemName=%s&returnUrl=%s".formatted(
                waitingHost,
                systemName,
                bookingHost);
    }

    // Index 접속 -> 티켓 유 -> 대기
    //            -> 티켓 무 -> 대기열 등록 -> 대기열 사이트로 이동

    // 추후 대기열 사이트에서 예약 사이트로 재접속 시 티켓의 유효성을 검증해야 한다
}
