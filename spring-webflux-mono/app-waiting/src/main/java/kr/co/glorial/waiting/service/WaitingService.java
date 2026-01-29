package kr.co.glorial.waiting.service;

import kr.co.glorial.waiting.lua.LuaScriptExecutor;
import kr.co.glorial.waiting.dto.request.AppendUserRequest;
import kr.co.glorial.waiting.dto.response.VerifyEntryKeyResponse;
import kr.co.glorial.waiting.dto.response.WaitingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class WaitingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final LuaScriptExecutor luaScriptExecutor;

    private final String USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "waiting:queue:*:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_ACTIVE_KEY = "waiting:queue:active:%s"; // 프로젝트, 도메인, 상태/속성, UUID

    // 대기열 등록
    public WaitingResponse appendWaiting(String identifier, String userId, long timestamp) {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        boolean result = redisTemplate.opsForZSet().add(key, userId, timestamp);
        if (!result) {
            throw new RuntimeException("생성 실패");
        }

        return WaitingResponse.builder().userId(userId).identifier(identifier).timestamp(timestamp).build();
    }

    // 대기열 순번 조회
    public long retrieveWaitRank(String identifier, String userId) {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        Long rank = redisTemplate.opsForZSet().rank(key, userId);

        if (rank == null) {
            throw new IllegalStateException("허가되지 않은 사용자입니다");
        }

        return rank + 1;
    }

    // 대기열 전체 순번 조회
    public long retrieveTotalRank(String identifier) {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        Long totalRank = redisTemplate.opsForZSet().zCard(key);

        if (totalRank == null) {
            return -1L;
        }

        return totalRank;
    }

    // 대기열 등록 + 순번
    public WaitingResponse appendWaitingAndRank(String identifier, long timestamp) {
        var userId = UUID.randomUUID().toString();
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);

        long rank = luaScriptExecutor.execute("redis/waiting_queue_append_and_rank.lua", ReturnType.INTEGER, Collections.singletonList(key), userId, String.valueOf(timestamp));
        rank += 1;

        return WaitingResponse.builder().userId(userId).identifier(identifier).timestamp(timestamp).position(rank).build();
    }

    // 대기열 등록 + 순번
    public WaitingResponse appendWaitingAndRank(AppendUserRequest appendUser) {
        return appendWaitingAndRank(appendUser.getSystemName(), appendUser.getTimestamp());
    }

    // 대기열 순번 + 전체 순번 조회
    public WaitingResponse retrieveWaitAndTotalRank(String identifier, String userId) {
        long rank = retrieveWaitRank(identifier, userId);
        long totalRank = retrieveTotalRank(identifier);

        return WaitingResponse.builder().allowed(false).position(rank).total(totalRank).userId(userId).build();
    }

    // 입장허가 조회
    public WaitingResponse retrieveEntryKey(String identifier, String userId) {
        String savedSystemName = redisTemplate.opsForValue().get(USER_QUEUE_ACTIVE_KEY.formatted(userId));
        if (savedSystemName == null) {
            return null;
        }

        if (!savedSystemName.equals(identifier)) {
            throw new IllegalStateException("허가되지 않은 사용자입니다");
        }

        // 입장허가된 사용자
        var returnUrl = UriComponentsBuilder
                .fromUriString(SystemReturnUrl.fromSiteName(savedSystemName).getUrl())
                .path("/")
                .queryParam("entryTicket", userId)
                .encode()
                .build()
                .toUriString();

        return WaitingResponse.builder().allowed(true).userId(userId).returnUrl(returnUrl).build();
    }

    // 입장허가 검증
    public VerifyEntryKeyResponse verifyEntryKey(String identifier, String userId) {
        String savedSystemName = redisTemplate.opsForValue().getAndDelete(USER_QUEUE_ACTIVE_KEY.formatted(userId));

        if (savedSystemName == null) {
            return VerifyEntryKeyResponse.builder().allowed(false).build();
        }

        if (!savedSystemName.equals(identifier)) {
            throw new IllegalStateException("허가되지 않은 사용자입니다");
        }

        return VerifyEntryKeyResponse.builder().allowed(true).build();
    }
}
