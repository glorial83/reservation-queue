package kr.co.glorial.waiting.controller;

import kr.co.glorial.waiting.dto.request.VerifyEntryKeyRequest;
import kr.co.glorial.waiting.dto.response.VerifyEntryKeyResponse;
import kr.co.glorial.waiting.dto.response.WaitingResponse;
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

    /**
     * 대기열 사용자의 실시간 상태(순번 또는 입장허가 여부) 조회.
     *
     * @param identifier
     * @param userId
     * @return
     */
    @GetMapping("position")
    public WaitingResponse position(@RequestParam(name = "identifier") String identifier, @RequestParam(name = "userId") String userId) {
        // 입장허가 사용자일경우
        WaitingResponse allowedInfo = service.retrieveEntryKey(identifier, userId);
        if (allowedInfo != null) {
            return allowedInfo;
        }

        // 대기열 사용자일경우
        return service.retrieveWaitAndTotalRank(identifier, userId);
    }

    /**
     * 입장토큰 검증.
     *
     * @param request
     * @return
     */
    @GetMapping("verify")
    public VerifyEntryKeyResponse verify(VerifyEntryKeyRequest request) {
        return service.verifyEntryKey(request.getIdentifier(), request.getUserId());
    }

}
