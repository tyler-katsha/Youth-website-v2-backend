local currentToken = redis.call('GET', KEYS[1])

-- Session doesn't exist / expired
if not currentToken then
    return 0
end

-- Refresh-token reuse detected
if currentToken ~= ARGV[1] then
    redis.call('DEL', KEYS[1])
    return -1
end

-- Rotate the token atomically
redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])

-- Return the new refresh-token secret
return ARGV[2]