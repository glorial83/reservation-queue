package kr.co.glorial.booking.service;

import kr.co.glorial.booking.dto.response.VerifyEntryKeyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    @Value("${waiting.host}")
    private String waitingHost;

    private String systemName = "booking";

    private final RestTemplate restTemplate;

    public boolean verifyEntryKey(String entryTicket) {
        var uri = UriComponentsBuilder
                .fromUriString(waitingHost)
                .path("/verify")
                .queryParam("userId", entryTicket)
                .queryParam("identifier", systemName)
                .encode()
                .build()
                .toUri();

        log.info("uri : {}", uri);

        ResponseEntity<VerifyEntryKeyResponse> response = restTemplate.getForEntity(uri, VerifyEntryKeyResponse.class);
        log.info(String.valueOf(response.getBody()));

        return response.getBody().isAllowed();
    }

}
