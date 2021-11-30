from flask import Flask, request,jsonify
from werkzeug.wrappers import response
from entity import *
from flask_apscheduler import APScheduler
from datetime import datetime
from redis_connection import redis_connection
from flask_cors import CORS
from mysql import stock_db, stock_price_history_db

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
def before_request():
    authorization = request.headers["authorization"]
    print(authorization)
    return None

@app.route('/', methods=['POST'])
def hello_world():
    user = UserVO(1, "min")
    vo = ListQueryVO([user], 3)
    response_entity = ResponseEntity.success(message=None, data=vo)
    print("processing")
    return response_entity.serialize()


@app.route("/stock/getEligibleStockList", methods=["POST"])
def get_eligible_stock_list():
    note_date = request.json["noteDate"]
    page_index = int(request.json["pageIndex"])
    page_size = int(request.json["pageSize"])
    total_count = stock_price_history_db.count_eliablge_stock_list(note_date)
    stock_list = stock_price_history_db.get_eliablge_stock_list(
        note_date, get_start(page_index,page_size), page_size) if total_count > 0 else []
    data = {
        "totalCount": total_count,
        "stockList": stock_list
    }
    response_entity = ResponseEntity.success(
        "暂无数据" if len(stock_list) == 0 else None, data)
    return response_entity.serialize()


def get_start(page_index: int, page_size: int):
    return (page_index - 1) * page_size


if __name__ == '__main__':
    # app.config.from_object(Config())

    # scheduler = APScheduler()
    # scheduler.init_app(app)
    # scheduler.start()
    app.run(port=8150, debug=True)
