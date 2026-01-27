# 예약 대기열

## Spring-Webflux-Mono

### 어플리케이션 구성

- app-waiting : 대기열 어플
- app-booking : 예약 어플
- redis : 대기열 저장소
  1. 대기열 ZSet
     - KEY : waiting:{도메인}:wait
     - VALUE : {사용자UUID}
     - SCORE : epochMilli

  2. 입장허용 String
     - KEY : waiting:queue:active:{사용자UUID}
     - VALUE : {도메인}
     - TTL : 3분

### 프로세스

```
     Index 접속 -> 티켓 유 -> 대기
                -> 티켓 무 -> 대기열 등록 -> 대기열 사이트로 이동

    아예 접속 하자마자 유/무 하지말고 그냥 대기열 사이트로 이동시키면 어떨까? ✔
     - 대기열에서 UUID가 관리되고 책임소재가 더 분명할 것 같은데? ✔
```

1. (app-booking) 접속
2. (app-booking) `entry-ticket` 쿠키 조회 ✔
3. (app-booking) `entry-ticket` 없다면 ✔
   1. (app-booking) 없다면 `app-waiting`으로 이동 ✔
   2. (app-waiting) 사용자 UUID 생성 ✔
   3. (app-waiting) 대기열 등록 + 대기열 등록정보 반환(+luaScript) ✔
      - 대기열 등록정보
        - `identifire` : 도메인
        - `userId` : 사용자UUID
        - `timestamp` : opchMilli
        - `position` : 대기열 순번
   4. (app-waiting) 대기 페이지로 이동 ✔
   5. (app-waiting) 사용자 UUID(userId) 로 대기열 1초단위 폴링 ✔
      1. 입장허용으로 사용자 UUID가 변경되었는지 체크 ✔
      2. 아직 대기중이라면 대기 순번 조회 ✔
      3. 둘다 아닐 경우 오류발생 ✔
         - 대기하던 중 새로고침 한 경우엔?
           - 대기에서 탈락됨 : `사용자 UUID`를 cookie나 storage에 저장하지 않는 휘발성이라 그렇다.
         - 대기하다가 나간 사람에 대한 처리는?
           - 일단 대기열에 잇다가 `active`로 전환될 것임

4. (app-booking) `entry-ticket` 있을 경우 ✔
5. (app-booking) `entry-ticket` 유효성 검증 ✔
   1. `app-waiting` 에 유효성 검증 요청 ✔
   2. 올바르지 않을 경우 `app-waiting`으로 이동 ✔
6. (app-booking) 예매 진행

### 대기열 관리 배치

1. 10초마다 대기열에서 `popMin` 을 진행 함 : 3건 ✔
2. pop된 사용자를 `active` queue(String)로 돌림 ✔

## TODO

- 대기열에 있다가 나간경우 처리 추가 : 어떻게 할 방법이 없는데....
- 예매하다가 TTL인경우 처리
- 예매하고 빠져나간 사람에 대한 처리도 하면 좀 더 리소스를 활용 할 수 있을 듯
- MONO로 전환
- Exception 처리
- 입장허용 후 `app-booking`으로 접속 시 새로운 token을 만드는 것 고려(예, 입장완료?)
- 도메인을 사이트단위가 아니라 공연별로 처리
- 예매 프로세스 구현
- 예매시 자리 정보 사전 등록(이것도 redis에 사전등록?)
  - 예매시 중복방지 방안(lua말곤 없을 듯)
  - redis가 없던 시절은 어떻게 구현했는지 검색해 볼 것
- f/e와 b/e가 완전히 나뉜경우에대한 고려

## 테스트 시작

http://127.0.0.1:8090/
