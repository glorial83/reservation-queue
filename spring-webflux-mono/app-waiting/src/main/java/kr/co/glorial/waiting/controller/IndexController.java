package kr.co.glorial.waiting.controller;

import kr.co.glorial.waiting.dto.request.AppendUserRequest;
import kr.co.glorial.waiting.dto.response.WaitingResponse;
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
    public String waiting(AppendUserRequest appendUser, Model model) {
        WaitingResponse waitingResponse = service.appendWaitingAndRank(appendUser);
        log.info("대기사용자:{}", waitingResponse);

        model.addAttribute("waitingResponse", waitingResponse);

        return "waiting";
    }
}