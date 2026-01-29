package kr.co.glorial.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/booking")
    public String booking(@RequestParam(required = false, defaultValue = "none", name = "systemName") String systemName) {
        return "booking";
    }

    // Index 접속 -> 티켓 유 -> 대기
    //            -> 티켓 무 -> 대기열 등록 -> 대기열 사이트로 이동

    // 추후 대기열 사이트에서 예약 사이트로 재접속 시 티켓의 유효성을 검증해야 한다
}
