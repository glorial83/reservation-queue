package kr.co.glorial.waiting.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class LuaScriptRegistry {

    private final RedisTemplate<String, String> redisTemplate;

    private final LuaScriptLoader loader;

    private final Map<String, String> luaScriptMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        registLuaScript("redis/waiting_queue_append_and_rank.lua");
    }

    private void registLuaScript(String scriptFile) {
        String script = loader.loadScript(scriptFile);
        String scriptSha = redisTemplate.execute((RedisCallback<String>) connection ->
                connection
                        .scriptingCommands()
                        .scriptLoad(script.getBytes())
        );

        luaScriptMap.put(scriptFile, scriptSha);
    }

    public String getLuaScript(String scriptFile) {
        String script = luaScriptMap.get(scriptFile);
        if (!StringUtils.hasText(script)) {
            throw new IllegalStateException("Lua script not registered: " + scriptFile);
        }

        return script;
    }
}
