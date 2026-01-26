package kr.co.glorial.waiting.controller;

import kr.co.glorial.waiting.WaitingInfo;
import kr.co.glorial.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class IndexController {

    private final WaitingService service;

    @GetMapping("waiting")
    public String waiting(AppendUserDTO appendUser, Model model) {
        WaitingInfo waitingInfo = service.appendWaitingAndRank(appendUser);
        log.info("대기사용자:{}", waitingInfo);

        model.addAttribute("waitingInfo", waitingInfo);
        model.addAttribute("returnUrl", appendUser.getReturnUrl());

        return "waiting";
    }
}
