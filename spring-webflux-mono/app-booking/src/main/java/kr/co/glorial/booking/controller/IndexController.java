package kr.co.glorial.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(@CookieValue(value = "waiting-ticket", defaultValue = "none") String waitingTicket) {
        // 대기페이지로 이동
        if ("none".equals(waitingTicket)) {
            log.debug("대기열 티켓이 없습니다.");
            return "waiting";
        }



        return "index";
    }

}
