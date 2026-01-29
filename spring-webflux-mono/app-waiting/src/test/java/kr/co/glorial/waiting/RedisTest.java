package kr.co.glorial.waiting;

import kr.co.glorial.waiting.lua.LuaScriptRegistry;
import kr.co.glorial.waiting.dto.response.WaitingResponse;
import kr.co.glorial.waiting.service.WaitingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class RedisTest {

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WaitingService service;

    @Autowired
    private LuaScriptRegistry luaScriptRegistry;

    private final String identifier = "default";

    @BeforeAll
    void before() {
        ReactiveRedisConnection reactiveRedisConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveRedisConnection.serverCommands().flushAll().block();

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.serverCommands().flushAll();
    }

    @Order(1)
    @Test
    void 등록_성공() {
        var time = Instant.now().toEpochMilli();
        var userId = "Normal_" + UUID.randomUUID().toString();

        WaitingResponse waitingResponse = service.appendWaiting(identifier, userId, time);
        log.info("사용자:{}", waitingResponse);

        long rank = service.retrieveWaitRank(identifier, userId);
        assertThat(rank).isGreaterThanOrEqualTo(0L);
        log.info("사용자 순번 :{}", rank);
    }

    @Order(2)
    @Test
    void LUA_등록() {
        String luaScriptText =
                "local key = KEYS[1]" +
                        "local userId = ARGV[1]" +
                        "local timestamp = ARGV[2]" +
                        "redis.call('ZADD', key, timestamp, userId)" +
                        "local rank = redis.call('ZRANK', key, userId)" +
                        "return rank";

        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(luaScriptText);
        luaScript.setResultType(Long.class);

        var time = Instant.now().toEpochMilli();
        var userId = "LUA_" + UUID.randomUUID().toString();

        String USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait";
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);

        long rank = redisTemplate.execute(luaScript, Collections.singletonList(key), userId, String.valueOf(time));
        rank += 1;

        log.info("새로운 순위?? : {}", rank);
    }

    @Order(3)
    @Test
    void LUA_FROM_REGISTRY_등록() {
        String script = luaScriptRegistry.getLuaScript("redis/waiting_queue_append_and_rank.lua");

        var time = Instant.now().toEpochMilli();
        var userId = "LUA_" + UUID.randomUUID().toString();

        String USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait";
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);

        long rank = redisTemplate.execute((RedisCallback<Long>) con ->
                con.scriptingCommands()
                        .evalSha(script, ReturnType.INTEGER, 1, key.getBytes(), userId.getBytes(), String.valueOf(time).getBytes())

        );

        rank += 1;

        log.info("새로운 순위?? : {}", rank);
    }

    @Order(4)
    @Test
    void LUA_FROM_EXECUTOR_등록() {
        var time = Instant.now().toEpochMilli();

        WaitingResponse waitingResponse = service.appendWaitingAndRank(identifier, time);
        log.info("사용자:{}", waitingResponse);
    }

    @Order(5)
    @Test
    void 전체갯수() {
        long totalRank = service.retrieveTotalRank(identifier);
        assertThat(totalRank).isGreaterThanOrEqualTo(1L);
        log.info("전체갯수:{}", totalRank);
    }

    @Order(6)
    @Test
    void 대기열_탈락() {
        var USER_QUEUE_WAIT_KEY = "waiting:queue:%s:wait";
        var key = USER_QUEUE_WAIT_KEY.formatted(identifier);
        var userId = UUID.randomUUID().toString();

        Long rank = redisTemplate.opsForZSet().rank(key, userId);
        assertThat(rank).isNull();
    }

}
