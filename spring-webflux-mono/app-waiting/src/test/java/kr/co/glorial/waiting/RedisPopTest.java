package kr.co.glorial.waiting;

import kr.co.glorial.waiting.service.WaitingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class RedisPopTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final String USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "waiting:queue:*:waitx"; // 프로젝트, 도메인, 식별자, 상태/속성
    private final String USER_QUEUE_ACTIVE_KEY = "waiting:queue:%s:active"; // 프로젝트, 도메인, 식별자, 상태/속성

    private final String identifier = "default";

    @Autowired
    private WaitingService service;

    @BeforeAll
    void before() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.serverCommands().flushAll();

        for (int i = 0; i < 20; i++) {
            var time = Instant.now().toEpochMilli();
            var userId = "Test_" + UUID.randomUUID();

            service.appendWaiting(identifier, userId, time);
        }
    }

    @Order(1)
    @Test
    void WAIT_사이트_조회() {
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);

        redisTemplate.scan(ScanOptions.scanOptions()
                        .match(key)
                        .count(30)
                        .build())
                .stream().map(info -> (Map.of("dddddddd", info)))
                //.stream().flatmap(info -> Stream.of(Map.of("dddddddd", info)))
                .forEach(System.out::println);
    }

    @Order(2)
    @DisplayName("pop된 사용자를 `active` queue로 돌림")
    @Test
    void WAIT_TO_ACTIVE() {
        var popSize = 3L;

        ScanOptions scanOptions = ScanOptions.scanOptions().match(USER_QUEUE_WAIT_KEY_FOR_SCAN).count(100).build();

        // 대기열 사용자 to 진행 사용자로 전환
        List<String> systemList;
        try (Cursor<String> cursors = redisTemplate.scan(scanOptions)) {
            systemList = cursors.stream().map(identifier -> identifier.split(":")[2])
                    .toList();
        }
        assertThat(systemList).size().isEqualTo(1);

        List<Boolean> moveResult = systemList.stream()
                .flatMap(systemName ->
                        redisTemplate.opsForZSet()
                                .popMin(USER_QUEUE_WAIT_KEY.formatted(identifier), popSize)
                                .stream().map(waitingQueue ->
                                        redisTemplate.opsForZSet().add(
                                                USER_QUEUE_ACTIVE_KEY.formatted(identifier),
                                                Objects.requireNonNull(waitingQueue.getValue()),
                                                Instant.now().toEpochMilli())
                                ))
                .toList();

        assertThat(moveResult).size().isEqualTo(popSize);
    }

    @Order(3)
    @DisplayName("pop된 사용자를 `active` queue로 돌림(서비스)")
    @Test
    void WAIT_TO_ACTIVE_SERVICE() {
        List<Boolean> moveResult = service.moveWaitToActive();

        assertThat(moveResult).size().isEqualTo(3L);
    }
}
