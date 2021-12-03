# from redis_connection import init_redis
from flask import Flask, request, jsonify
from werkzeug.security import generate_password_hash,check_password_hash
from response_entity import *
from flask_apscheduler import APScheduler
from datetime import datetime
from flask_cors import CORS
from mysql import stock_db, stock_price_history_db, init_db
import entity
from config import jwt_config

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


@app.route('/', methods=['POST'])
def hello_world():
    response_entity = ResponseEntity.success()
    return response_entity.serialize()


@app.route("/stock/getEligibleStockList", methods=["POST"])
def get_eligible_stock_list():
    note_date = request.json["noteDate"]
    page_index = int(request.json["pageIndex"])
    page_size = int(request.json["pageSize"])
    result = stock_price_history_db.get_eliablge_stock_list(
        note_date, get_start(page_index, page_size), page_size)
    total_count = stock_price_history_db.count_eliablge_stock_list(note_date)
    stock_list = stock_price_history_db.get_eliablge_stock_list(
        note_date, get_start(page_index,page_size), page_size) if total_count > 0 else []
    
    stocks = [{
            "id":int(item[0].id),
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
        "stockList": stocks
    }
    response_entity = ResponseEntity.success(
        "暂无数据" if len(stock_list) == 0 else None, data)
    return response_entity.serialize()


@app.route("/testSqlalchemy", methods=["POST"])
def test_sqlalchemy():
    q = entity.test()
    return jsonify(q), 200


@app.route("/getStockList", methods=["POST"])
def get_stock_list():
    results = stock_db.get_stock_list(0)
    result = [{
        "id": int(item.id),
        "stockNickCode": str(item.stock_nick_code)
    } for item in results]
    response_entity = ResponseEntity.success(result)
    return response_entity.serialize()




def get_start(page_index: int, page_size: int):
    return (page_index - 1) * page_size


if __name__ == '__main__':
    # app.config.from_object(Config())

    # scheduler = APScheduler()
    # scheduler.init_app(app)
    # scheduler.start()
    init_db()
    # init_redis()
    app.run(port=8150, debug=True)
    # h = generate_password_hash("minnan35")
    # print(check_password_hash(h, "adsf"))
    # print(generate_password_hash("minnan35"))
