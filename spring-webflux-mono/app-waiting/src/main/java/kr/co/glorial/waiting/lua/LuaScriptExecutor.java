package kr.co.glorial.waiting.lua;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LuaScriptExecutor {

    private final LuaScriptRegistry luaScriptRegistry;
    private final RedisTemplate<String, String> redisTemplate;

    public <T> T execute(String luaScript, ReturnType returnType, List<?> keys, Object... args) {
        T result = redisTemplate.execute((RedisCallback<T>) con ->
                con.scriptingCommands()
                        .evalSha(
                                luaScriptRegistry.getLuaScript(luaScript),
                                returnType,
                                keys.size(),
                                keysAndArgs(redisTemplate.getValueSerializer(), keys, args)
                        )
        );

        return Optional.ofNullable(result)
                .orElseThrow(() ->
                        new IllegalStateException("Redis returned null"));
    }

    @SuppressWarnings("unchecked")
    protected byte[][] keysAndArgs(RedisSerializer<?> argsSerializer, List<?> keys, Object[] args) {
        RedisSerializer<Object> objSerializer = (RedisSerializer<Object>) argsSerializer;

        int keySize = keys != null ? keys.size() : 0;
        byte[][] keysAndArgs = new byte[args.length + keySize][];
        int i = 0;
        if (keys != null) {
            for (Object key : keys) {
                if (objSerializer == null && key instanceof byte[]) {
                    byte[] keyBytes = (byte[]) key;
                    keysAndArgs[i++] = keyBytes;
                } else {
                    keysAndArgs[i++] = objSerializer.serialize(key);
                }
            }
        }

        for (Object arg : args) {
            if (objSerializer == null && arg instanceof byte[] argBytes) {
                keysAndArgs[i++] = argBytes;
            } else {
                keysAndArgs[i++] = objSerializer.serialize(arg);
            }
        }

        return keysAndArgs;
    }
}
