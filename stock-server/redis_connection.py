import redis
from config import redis_config

rc

def init_redis():
    global rc
    host = redis_config["host"]
    port = redis_config["port"]
    password = redis_config["password"]
    db = redis_config["database"]
    rc = redis.StrictRedis(host=host,port=port,decode_responses=True, password=password, db=db)