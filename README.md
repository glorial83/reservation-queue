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
2. (app-booking) 대기열 등록 요청
3. (app-waiting) 대기열 등록 + 대기열 등록정보 반환
   - 대기열 등록정보
     - `identifire` : 도메인
     - `userId` : 사용자UUID
     - `timestamp` : opchMilli
     - `position` : 대기열 순번
4.
