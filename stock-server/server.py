# from redis_connection import init_redis
from flask import Flask, request, jsonify, session
from sqlalchemy.sql.functions import user
from werkzeug.security import generate_password_hash, check_password_hash
from response_entity import *
from flask_apscheduler import APScheduler
from datetime import datetime
from flask_cors import CORS
from mysql import init_db, stock_db, stock_price_history_db, auth_user_db
from entity import *
from config import jwt_config
import uuid
import os
from dto import *
import time
import jwt

app = Flask(__name__)
CORS(app, resources=r"/*")


class Config(object):
    JOBS = [
        {
            "id": "job1",
            "func": "__main__:daily_task",
            "args": (),
            "trigger": "cron",
            "minute": 54
        }
    ]


def daily_task():
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(now)


@app.before_request
def authorization_filter():

    return None


@app.route('/', methods=['POST'])
def hello_world():
    response_entity = ResponseEntity.success()
    return response_entity.serialize()


@app.route("/stock/getEligibleStockList", methods=["POST"])
def get_eligible_stock_list():
    dto = GetEligibleStockList(request)
    validate_msg = dto.validate()
    if(len(validate_msg) > 0):
        return ResponseEntity.invalid_param(data=validate_msg).serialize()
    total_count = stock_price_history_db.count_eliablge_stock_list(dto.noteDate())
    stock_list = stock_price_history_db.get_eliablge_stock_list(
        dto.noteDate(), dto.get_start(), dto.pageSize()) if total_count > 0 else []

    stocks = [{
        "id": int(item[0].id),
        "sotckId": int(item[0].stock_id),
        "stockName": str(item.stock_name),
        "stockCode":str(item.stock_code),
        "startPrice":str(item[0].start_price),
        "endPrice":str(item[0].end_price),
        "highestPrice":str(item[0].highest_price),
        "lowestPrice":str(item[0].lowest_price),
        "avgPricePast120Days":str(item[0].avg_price_past_120_days),
        "createTime":str(item[0].create_time)
    } for item in stock_list]
    data = {
        "totalCount": total_count,
        "list": stocks
    }
    response_entity = ResponseEntity.success(
        data, "暂无数据" if len(stock_list) == 0 else None)
    return response_entity.serialize()



@app.route("/stock/auth/login", methods=["POST"])
def login():
    dto = LoginDTO(request)
    validate = dto.validate()
    if len(validate) > 0:
        return ResponseEntity.invalid_param(data=validate).serialize()
    
    auth_user = auth_user_db.get_user_by_username(dto.username())
    if(auth_user == None):
        return ResponseEntity.fail(message="用户不存在").serialize()
    check = check_password_hash(str(auth_user.password), dto.password())
    if(not check):
        return ResponseEntity.fail(message="密码错误").serialize()

    exp = time.time() + 1000 * 60 * 60 * 24
    payload = {
        "exp":exp,
        "data":{
            "id":int(auth_user.id),
            "username":str(auth_user.username),
            "passwordStamp":str(auth_user.password_stamp),
        }
    }
    key = jwt_config["secret"]
    token = jwt.encode(payload=payload, key=key, algorithm="HS256")

    data = {
        "token":token
    }
    return ResponseEntity.success(data=data).serialize()


def get_start(page_index: int, page_size: int):
    return (page_index - 1) * page_size


if __name__ == '__main__':
    # app.config.from_object(Config())

    # scheduler = APScheduler()
    # scheduler.init_app(app)
    # scheduler.start()
    init_db()
    # init_redis()
    app.secret_key = os.urandom(24)
    app.run(port=8150, debug=True)
    # h = generate_password_hash("minnan35")
    # print(check_password_hash(h, "adsf"))
    # print(generate_password_hash("minnan35"))
