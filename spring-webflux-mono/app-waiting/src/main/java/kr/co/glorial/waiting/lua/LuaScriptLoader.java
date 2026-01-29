package kr.co.glorial.waiting.lua;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class LuaScriptLoader {

    public String loadScript(String file) {
        ClassPathResource resource = new ClassPathResource(file);
        try {
            return new String(resource.getContentAsByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
