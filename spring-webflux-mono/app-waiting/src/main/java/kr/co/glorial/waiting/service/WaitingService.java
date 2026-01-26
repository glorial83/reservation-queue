package kr.co.glorial.waiting.service;

import kr.co.glorial.waiting.WaitingInfo;
import kr.co.glorial.waiting.config.LuaScriptExecutor;
import kr.co.glorial.waiting.controller.AppendUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private final RedisTemplate<String, String> redisTemplate;
    private final LuaScriptExecutor luaScriptExecutor;

    private final String USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "waiting:queue:*:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_PROCEED_KEY = "waiting:queue:%s:proceed"; // 프로젝트, 도메인, 식별자, 상태/속성

    // 대기열 등록
    public WaitingInfo appendWaiting(String identifier, String userId, long timestamp) {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        boolean result = redisTemplate.opsForZSet().add(key, userId, timestamp);
        if (!result) {
            throw new RuntimeException("생성 실패");
        }

        return WaitingInfo.builder().userId(userId).identifier(identifier).timestamp(timestamp).build();
    }

    // 대기열 순번 조회
    public long retrieveWaitRank(String identifier, String userId) {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        Long rank = redisTemplate.opsForZSet().rank(key, userId);

        if (rank == null) {
            return -1L;
        }

        return rank + 1;
    }

    public long retrieveTotalRank(String identifier) {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        Long totalRank = redisTemplate.opsForZSet().zCard(key);

        if (totalRank == null) {
            return -1L;
        }

        return totalRank;
    }

    // 대기열 등록 + 순번
    public WaitingInfo appendWaitingAndRank(String identifier, long timestamp) {
        var userId = UUID.randomUUID().toString();
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);

        long rank = luaScriptExecutor.execute("redis/waiting_queue_append_and_rank.lua", ReturnType.INTEGER, Collections.singletonList(key), userId, String.valueOf(timestamp));
        rank += 1;

        return WaitingInfo.builder().userId(userId).identifier(identifier).timestamp(timestamp).position(rank).build();
    }

    // 대기열 등록 + 순번
    public WaitingInfo appendWaitingAndRank(AppendUserDTO appendUser) {
        return appendWaitingAndRank(appendUser.getSystemName(), appendUser.getTimestamp());
    }
}
