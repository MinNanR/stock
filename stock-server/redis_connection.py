import redis

redis_connection = redis.StrictRedis(host="minnan.site", port=6379, decode_responses=True, password="minnan", db=3)