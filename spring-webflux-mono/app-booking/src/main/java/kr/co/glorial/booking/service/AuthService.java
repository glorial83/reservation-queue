package kr.co.glorial.booking.service;

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

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkEntryTicket(String entryTicket) {
        var uri = UriComponentsBuilder
                .fromUriString("http://127.0.0.1:8091")
                .path("/verify")
                .queryParam("userId", entryTicket)
                .queryParam("systemName", "booking")
                .encode()
                .build()
                .toUri();

        log.info("uri : {}", uri);

        ResponseEntity<WaitingInfo> response = restTemplate.getForEntity(uri, WaitingInfo.class);
        log.info(response.getBody().toString());

        return response.getBody().isAllowed();
    }

}
