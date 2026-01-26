package kr.co.glorial.waiting.controller;

import kr.co.glorial.waiting.WaitingInfo;
import kr.co.glorial.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WaitingController {

    private final WaitingService service;

    @GetMapping("position")
    public WaitingInfo position(String identifier, String userId) {
        log.info("userId================> : {}", userId);

        long rank = service.retrieveWaitRank(identifier, userId);
        long totalRank = service.retrieveTotalRank(identifier);

        return WaitingInfo.builder().position(rank).total(totalRank).userId(userId).build();
    }

}
