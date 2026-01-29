package kr.co.glorial.booking.controller;

import kr.co.glorial.booking.dto.response.VerifyEntryKeyResponse;
import kr.co.glorial.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BookingController {

    @Value("${waiting.host}")
    private String waitingHost;

    @Value("${booking.host}")
    private String bookingHost;

    private String systemName = "booking";

    private final AuthService service;

    @GetMapping("/api/check")
    public VerifyEntryKeyResponse check(@RequestParam(required = false, defaultValue = "none", name = "entryTicket") String entryTicket) {
        // 예매페이지로 이동
        if (!"none".equals(entryTicket) && service.verifyEntryKey(entryTicket)) {
            String returnUrl = "%s/booking?systemName=%s".formatted(
                    bookingHost,
                    systemName);

            //response로 header에 새로운 token을 발급해준다

            return VerifyEntryKeyResponse.builder().allowed(true).returnUrl(returnUrl).build();
        }

        // 대기사이트로 이동
        log.info("입장 티켓이 없습니다.");
        String returnUrl = "%s/waiting?systemName=%s&entryTicket=%s".formatted(
                waitingHost,
                systemName,
                entryTicket);

        return VerifyEntryKeyResponse.builder().allowed(false).returnUrl(returnUrl).build();
    }

}
