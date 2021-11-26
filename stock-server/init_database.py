from pymysql import thread_safe
from mysql import *
from redis_connection import redis_connection
import requests
from entity import *
from threading import Thread
import time
import datetime
import numpy as np


# 初始化股票列表
def init_stock_info():
    response = requests.get("https://api.doctorxiong.club/v1/stock/all")
    response_json = response.json()
    data = response_json["data"]
    stock_list = [StockInfo(id=None, stock_name=item[1],
                            stock_nick_code=item[0], stock_code=None, detected=1) for item in data]
    i = 0
    while(True):
        if((i+1) * 1000 > len(stock_list)):
            data_to_insert = stock_list[i*1000:]
            stock_db.inset_stock_batch(data_to_insert)
            break
        else:
            data_to_insert = stock_list[i*1000:(i+1) * 1000]
            stock_db.inset_stock_batch(data_to_insert)
        i = i+1


url = "https://api.doctorxiong.club/v1/stock/kline/day?token=LJUjbTGJeO&code={0}&startDate=2021-01-01&type=1"
#
# 查询今年的数据历史数据


def init_stock_price_history(process_name: str):
    while(True):
        redis_data = redis_connection.lpop("stock_list")
        if(redis_data == None):
            finsihed = redis_connection.exists("finished")
            if(finsihed == 0):
                break
            else:
                time.sleep(1)
        else:
            stock_info = json.loads(redis_data)
            stock_code = stock_info["stock_nick_code"]
            print("{0} : begin to work, target stock code = {1}".format(
                process_name, stock_code))
            handle_single_stock(stock_info)
            print("{0} : success to save history data, sotck code = {1}".format(
                process_name, stock_code))


def handle_single_stock(stock_info: map):
    stock_id = stock_info["id"]
    code = stock_info["stock_nick_code"]
    full_url = url.format(code)
    response = requests.get(full_url)
    response_json = response.json()
    if(response_json["code"] != 200):
        return

    response_data = response_json["data"]
    history_list = []
    past_120 = []
    last_avg_price = None
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    for i in range(0, len(response_data)):
        item = response_data[i]
        history = StockPriceHistory()
        history.set_stock_id(stock_id)
        history.set_start_price(item[1])
        history.set_end_price(item[2])
        history.set_highest_price(item[3])
        history.set_lowest_price(item[4])
        history.set_volume(int(item[5].split(".")[0]))
        history.set_end_price_last(response_data[i-1][2] if i > 0 else None)
        history.set_note_date(item[0])
        history.set_create_time(now)
        past_120.append(float(item[3]))
        if(i >= 119):
            avg_price = str(round(np.mean(past_120), 4))
            history.set_avg_price_past_120_days_last(last_avg_price)
            history.set_avg_price_past_120_days(avg_price)
            last_avg_price = avg_price
            past_120.pop(0)
        else:
            history.set_avg_price_past_120_days(None)
            history.set_avg_price_past_120_days_last(None)
        history_list.append(history)
    stock_price_history_db.insert_batch(history_list)


def init_data_to_process():
    start = 0
    while(True):
        stock_list = stock_db.get_stock_list(start)
        print(len(stock_list))
        for stock in stock_list:
            m = {"id": stock.id, "stock_nick_code": stock.stockNickCode}
            json_string = json.dumps(m)
            redis_connection.rpush("stock_list", json_string)

        start = start+1000
        if(len(stock_list) < 1000):
            break

    redis_connection.delete("finished")


def calculate(lock: threading.Lock):
    while(True):
        redis_data = redis_connection.lpop("stock_list")
        if(redis_data == None):
            break
        stock_info = json.loads(redis_data)
        stock_id = int(stock_info["id"])
        stock_price_history_db.calculate(stock_id, lock)


def recover_redis():
    s1 = {"id": 1, "stock_nick_code": "sh600000"}
    s2 = {"id": 2, "stock_nick_code": "sh600001"}
    s3 = {"id": 3, "stock_nick_code": "sh600002"}
    s4 = {"id": 4, "stock_nick_code": "sh600003"}
    redis_connection.lpush("stock_list", json.dumps(s4))
    redis_connection.lpush("stock_list", json.dumps(s3))
    redis_connection.lpush("stock_list", json.dumps(s2))
    redis_connection.lpush("stock_list", json.dumps(s1))


if __name__ == "__main__":
    # redis_connection.set("finished", 1)
    # init_data_to_process()
    lock = threading.Lock()
    # stock_price_history_db.calculate(1, lock)
    # handle_single_stock({"id": "1", "stock_nick_code": "sh600000"}, lock)
    t1 = Thread(target=init_stock_price_history, args=("Thread1", lock,))
    t2 = Thread(target=init_stock_price_history, args=("Thread2",lock,))
    t3 = Thread(target=init_stock_price_history, args=("Trread3",lock,))

    # recover_redis()
    t1.start()
    t2.start()
    t3.start()

    t1.join()
    t2.join()
    t3.join()
