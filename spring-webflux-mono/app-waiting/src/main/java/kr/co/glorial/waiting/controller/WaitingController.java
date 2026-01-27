package kr.co.glorial.waiting.controller;

import kr.co.glorial.waiting.WaitingInfo;
import kr.co.glorial.waiting.service.SystemReturnUrl;
import kr.co.glorial.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WaitingController {

    private final WaitingService service;

    @GetMapping("position")
    public WaitingInfo position(@RequestParam(name = "identifier") String identifier, @RequestParam(name = "userId") String userId) {
        // 입장허가 조회
        String returnSystemName = service.retrieveEntryKey(identifier, userId);
        if (returnSystemName != null) {
            // 입장허가된 사용자
            String returnUrl = SystemReturnUrl.fromSiteName(returnSystemName).getUrl();
            return WaitingInfo.builder().allowed(true).userId(userId).returnUrl(returnUrl).build();
        }

        // 대기열 사용자
        long rank = service.retrieveWaitRank(identifier, userId);
        long totalRank = service.retrieveTotalRank(identifier);

        return WaitingInfo.builder().allowed(false).position(rank).total(totalRank).userId(userId).build();
    }

    @GetMapping("verify")
    public WaitingInfo verify(VerifyUserDTO verifyUser) {
        String returnSystemName = service.retrieveEntryKey(verifyUser.getSystemName(),verifyUser.getUserId());
        if (returnSystemName == null) {
            return WaitingInfo.builder().allowed(false).build();
        }

        return WaitingInfo.builder().allowed(true).build();
    }

}
