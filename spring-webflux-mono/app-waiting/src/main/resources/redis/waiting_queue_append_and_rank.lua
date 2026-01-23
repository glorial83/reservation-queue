--[[
  waiting_queue_append_and_rank.lua - ZSet 대기열 등록과 랭크 조회를 한번에 처리
  @KEYS[1]: 대기열의 Redis Key (ex: "waiting:queue:default:wait")
  @ARGV[1]: 사용자 UUID
  @ARGV[1]: epochMilli

  동작:
    - (1) 대기열 등록
    - (2) 사용자의 랭크 조회
    - return: 랭크
]]
-- 락의 값이 내 값인지 확인

local key = KEYS[1]
local userId = ARGV[1]
local timestamp = ARGV[2]

redis.call('ZADD', key, timestamp, userId)
local rank = redis.call('ZRANK', key, userId)

return rank