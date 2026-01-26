package kr.co.glorial.waiting.service;

import kr.co.glorial.waiting.WaitingInfo;
import kr.co.glorial.waiting.config.LuaScriptExecutor;
import kr.co.glorial.waiting.controller.AppendUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

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
    public long retrieveWaitRank(WaitingInfo waitingInfo) {
        var key = USER_QUEUE_WAIT_KEY.formatted(waitingInfo.getIdentifier());
        Long rank = redisTemplate.opsForZSet().rank(key, waitingInfo.getUserId());

        if (rank == null) {
            return -1L;
        }

        return rank + 1;
    }

    // 대기열 등록 + 순번
    public WaitingInfo appendWaitingAndRank(String identifier, String userId, long timestamp) {
        var time = Instant.now().toEpochMilli();
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);

        long rank = luaScriptExecutor.execute("redis/waiting_queue_append_and_rank.lua", ReturnType.INTEGER, Collections.singletonList(key), userId, String.valueOf(time));
        rank += 1;

        return WaitingInfo.builder().userId(userId).identifier(identifier).timestamp(timestamp).position(rank).build();
    }

    public WaitingInfo appendWaitingAndRank(AppendUserDTO appendUser) {
        var key = USER_QUEUE_WAIT_KEY.formatted(appendUser.getSystemName());

        long rank = luaScriptExecutor.execute("redis/waiting_queue_append_and_rank.lua", ReturnType.INTEGER,
                Collections.singletonList(key),
                appendUser.getUserId(),
                String.valueOf(appendUser.getTimestamp()));
        rank += 1;

        return WaitingInfo.builder()
                .userId(appendUser.getUserId())
                .identifier(appendUser.getSystemName())
                .timestamp(appendUser.getTimestamp())
                .position(rank).build();
    }
}
