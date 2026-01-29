package kr.co.glorial.waiting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class WaitingBatchService {

    private final RedisTemplate<String, String> redisTemplate;

    private final String USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "waiting:queue:*:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_ACTIVE_KEY = "waiting:queue:active:%s"; // 프로젝트, 도메인, 상태/속성, UUID

    private final Long MAX_POP_SIZE = 3L;

    // 대기열 POP -> 진행열 ADD, 10초마다 실행
    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public List<Boolean> moveWaitToActive() {
        //대기열 SCAN 해서 systemName 가져오기(여러 사이트에서 사용중일 수 있으므로)
        ScanOptions scanOptions = ScanOptions.scanOptions().match(USER_QUEUE_WAIT_KEY_FOR_SCAN).count(100).build();

        List<String> systemList;
        try (Cursor<String> cursors = redisTemplate.scan(scanOptions)) {
            systemList = cursors.stream().map(identifier -> identifier.split(":")[2])
                    .toList();
        }

        List<Boolean> moveResult = systemList.stream()
                .flatMap(identifier ->
                        redisTemplate.opsForZSet()
                                .popMin(USER_QUEUE_WAIT_KEY.formatted(identifier), MAX_POP_SIZE)
                                .stream()
                                .map(waitingQueue -> {
                                            redisTemplate.opsForValue().set(
                                                    USER_QUEUE_ACTIVE_KEY.formatted(Objects.requireNonNull(waitingQueue.getValue())),
                                                    identifier,
                                                    1, TimeUnit.MINUTES);
                                            return true;
                                        }
                                ))
                .toList();

        log.info("이동결과 : {}", moveResult);

        return moveResult;
    }

}
