# 예약 대기열

## Spring-Webflux-Mono

### 어플리케이션 구성

- app-waiting : 대기열 어플
- app-booking : 예약 어플
- redis : 대기열 저장소

#### Redis

1. 대기열 ZSet
   - KEY : waiting:{도메인}:{사용자UUID}:wait
   - VALUE : {사용자UUID}
   - SCORE : epochMilli

### 프로세스

1. (app-booking) 접속
2. (app-booking) `entry-ticket` 쿠키 조회 ✔
3. (app-booking) `entry-ticket` 없다면
   1. (app-booking) 없다면 `app-waiting`으로 이동
   2. (app-waiting) 사용자 UUID 생성 ✔
   3. (app-waiting) 대기열 등록 + 대기열 등록정보 반환(+luaScript) ✔
      - 대기열 등록정보
        - `identifire` : 도메인
        - `userId` : 사용자UUID
        - `timestamp` : opchMilli
        - `position` : 대기열 순번
   4. (app-waiting) 대기 페이지로 이동 ✔
   5. (app-waiting) 3초마다 대기순번 Polling ✔

   6. (app-waiting) 사용자 UUID(userId) 가 유효한지 체크
      1. 유효하다면 대기페이지로 이동
      2. 유효하지 않을경우?
      3. 대기가 아니라 active 사용자일 경우?
      4. 대기하던 중 새로고침 한 경우엔? -> 대기에서 탈락하는 경우가 대부분인데 왜 : cookie나 storage에 저장하지 않는다 그래서 휘발성이라 그렇다.
   7. (app-waiting) 대기 페이지로 이동
   8. (app-waiting) 사용자 UUID(userId) 로 대기열 1분단위 폴링

4. (app-booking) `entry-ticket` 있을 경우
5. (app-booking) `entry-ticket` 유효성 검증
   1. `app-waiting` 에 유효성 검증 요청
   2. 올바르지 않을 경우 `app-waiting`으로 이동
6. (app-booking) 예매 진행

```
     Index 접속 -> 티켓 유 -> 대기
                -> 티켓 무 -> 대기열 등록 -> 대기열 사이트로 이동

    추후 대기열 사이트에서 예약 사이트로 재접속 시 티켓의 유효성을 검증해야 한다
    아예 접속 하자마자 유/무 하지말고 그냥 대기열 사이트로 이동시키면 어떨까?
     - 대기열에서 UUID가 관리되고 책임소재가 더 분명할 것 같은데?
     - 대기가 끝나고 UUID
```
