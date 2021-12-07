import redis
from config import redis_config


def Singleton(cls):
    _instance ={}

    def _singleton(*args, **kwargs):
        if cls not in _instance:
            _instance[cls] = cls(*args, **kwargs)
        return _instance[cls]
    return _singleton

@Singleton
class RedisConnection:
    def __init__(self):
        host = redis_config["host"]
        port = redis_config["port"]
        password = redis_config["password"]
        db = redis_config["database"]
        self.rc = redis.StrictRedis(host=host,port=port,decode_responses=True, password=password, db=db)    

    def get_connection(self):
        return self.rc
