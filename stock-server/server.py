from logging import debug
from flask import Flask, request, jsonify, session, abort
from sqlalchemy.sql.functions import user
from werkzeug.security import generate_password_hash, check_password_hash
from response_entity import *
from flask_apscheduler import APScheduler, scheduler
from datetime import datetime, timedelta
from flask_cors import CORS
from mysql import init_db, stock_db, stock_price_history_db, auth_user_db
from redis_connection import RedisConnection
from entity import *
from config import jwt_config
import uuid
import os
from dto import *
import time
import jwt
from task import get_today_price

class Config(object):
    JOBS=[
        {
            "id":"daily_stock_task",
            "func": get_today_price,
            'trigger':'cron',
            'hour': 15,
            "minute":30,
        }
    ]

app = Flask(__name__)
CORS(app, resources=r"/*")

auth_path = jwt_config["authpath"]
rec = RedisConnection()


@app.before_request
def option_filter():
    if str.upper(request.method) == "OPTIONS":
        return "ok", 200


@app.before_request
def authorization_filter():

    url = request.path
    if url in auth_path:
        return None

    token = request.headers.get("Authorization")
    if (token is None) or (not token.startswith("Bearer ")):
        abort(401)

    payload = jwt.decode(token[7:], key=jwt_config["secret"], algorithms=["HS256"])
    user = load_user_by_username(payload["data"]["username"])
    if (user == None) or user["password_stamp"] != payload["data"]["passwordStamp"]:
        abort(401)

    session["principal"] = user
    return None


@app.route("/stock/getEligibleStockList", methods=["POST"])
def get_eligible_stock_list():
    dto = GetEligibleStockListDTO(request)
    validate_msg = dto.validate()
    if len(validate_msg) > 0:
        return ResponseEntity.invalid_param(data=validate_msg).serialize()
    rc = rec.get_connection()
    if rc.get("lock") != None:
        return ResponseEntity.fail(message="数据统计中").serialize()

    total_count = stock_price_history_db.count_eliablge_stock_list(dto.noteDate())
    stock_list = (
        stock_price_history_db.get_eliablge_stock_list(
            dto.noteDate(), dto.get_start(), dto.pageSize()
        )
        if total_count > 0
        else []
    )

    stocks = [
        {
            "id": int(item[0].id),
            "stockId": int(item[0].stock_id),
            "stockName": str(item.stock_name),
            "stockCode": str(item.stock_code),
            "startPrice": str(item[0].start_price),
            "endPrice": str(item[0].end_price),
            "highestPrice": str(item[0].highest_price),
            "lowestPrice": str(item[0].lowest_price),
            "avgPricePast120Days": str(item[0].avg_price_past_120_days),
            "createTime": str(item[0].create_time),
        }
        for item in stock_list
    ]
    data = {"totalCount": total_count, "list": stocks}
    response_entity = ResponseEntity.success(
        data, "暂无数据" if len(stock_list) == 0 else None
    )
    return response_entity.serialize()


@app.route("/stock/getKLineData", methods=["POST"])
def get_k_line_data():
    dto = GetKlineDataDTO(request)
    validate_msg = dto.validate()
    if len(validate_msg) > 0:
        return ResponseEntity.invalid_param(data=validate_msg).serialize()

    raw_data = stock_price_history_db.get_k_line_data(dto.id())
    dates = [str(item.note_date) for item in raw_data]
    k_line_data = [
        [
            float(item.start_price),
            float(item.end_price),
            float(item.lowest_price),
            float(item.highest_price),
        ]
        for item in raw_data
    ]

    avg_line_data = [
        float(item.avg_price_past_120_days) if item.avg_price_past_120_days else None
        for item in raw_data
    ]
    reulst = {"dates": dates, "kLineData": k_line_data, "avgLineData": avg_line_data}
    return ResponseEntity.success(data=reulst).serialize()


@app.route("/stock/auth/login", methods=["POST"])
def login():
    dto = LoginDTO(request)
    validate = dto.validate()
    if len(validate) > 0:
        return ResponseEntity.invalid_param(data=validate).serialize()

    auth_user = load_user_by_username(dto.username())
    if auth_user == None:
        return ResponseEntity.fail(message="用户不存在").serialize()
    check = check_password_hash(str(auth_user["password"]), dto.password())
    if not check:
        return ResponseEntity.fail(message="密码错误").serialize()

    exp = time.time() + 1000 * 60 * 60 * 24
    payload = {
        "exp": exp,
        "data": {
            "id": int(auth_user["id"]),
            "username": str(auth_user["username"]),
            "passwordStamp": str(auth_user["password_stamp"]),
        },
    }
    key = jwt_config["secret"]
    token = jwt.encode(payload=payload, key=key, algorithm="HS256")

    data = {"token": token}
    return jsonify(ResponseEntity.success(data=data).serialize())


@app.route("/stock/auth/getUserInfo", methods=["POST"])
def get_user_info():
    principal = session.get("principal")
    data = {
        "user": {
            "username": principal["username"],
            "nickName": principal["nick_name"],
        }
    }
    print(data)
    return ResponseEntity.success(data=data).serialize()


def get_start(page_index: int, page_size: int):
    return (page_index - 1) * page_size


def load_user_by_username(username: str):
    rc = rec.get_connection()
    auth_user = rc.get("authUser:" + username)
    if auth_user != None:
        return json.loads(auth_user)
    auth_user = auth_user_db.get_user_by_username(username)
    if auth_user != None:
        u = {
            "id": int(auth_user.id),
            "username": str(auth_user.username),
            "password": str(auth_user.password),
            "password_stamp": str(auth_user.password_stamp),
            "role": str(auth_user.role),
            "nick_name": str(auth_user.nick_name),
        }
        rc.set(
            "authUser:" + str(auth_user.username), json.dumps(u), ex=timedelta(hours=1)
        )
        return u
    return None


if __name__ == "__main__":
    app.config.from_object(Config())
    scheduler = APScheduler()
    scheduler.init_app(app)
    scheduler.start()
    init_db()

    app.secret_key = os.urandom(24)
    app.run(port=8150, debug=True, use_reloader=False)
    # auth_user = AuthUser()
    # auth_user.username = "Leo"
    # auth_user.password = generate_password_hash("2b7ec156d236ae2b942f28a5391bca76")
    # auth_user.password_stamp = uuid.uuid4().hex
    # auth_user.nick_name = "Leo"
    # auth_user.role = "USER"
    # auth_user.create_time = datetime.now()
    # auth_user.update_time = datetime.now()
    # auth_user_db.add_user(auth_user)
