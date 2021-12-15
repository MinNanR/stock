from datetime import datetime
import requests

from requests.api import request
from mysql import stock_db, stock_price_history_db, init_db
from redis_connection import RedisConnection
import threading
from entity import StockPriceHistory, StockInfo
import time

from response_entity import ResponseEntity


def do_get_today_price(stock_list: list, date_str: str):
    history_list = []
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    for stock in stock_list:
        stock_code = stock.stock_nick_code
        history = handle_single_stock(stock_code, date_str)
        if history != None:
            history.stock_id = stock.id
            history.create_time = now
            history_list.append(history)
    stock_price_history_db.save_dailiy_data(history_list)


def handle_single_stock(stock_code: str, date_str: str):
    try:
        url = "https://api.doctorxiong.club/v1/stock/kline/day?token=LJUjbTGJeO&code={0}&startDate={1}&endDate={2}&type=1"
        full_url = url.format(stock_code, date_str, date_str)
        response = requests.get(full_url)
        response_json = response.json()
        print(response_json)
        if(response_json["code"] != 200 or response_json["data"] == None):
            return None
        if(len(response_json["data"]) == 0):
            return None

        item = response_json["data"][0]
        history = StockPriceHistory(start_price=item[1], end_price=item[2], highest_price=item[3], lowest_price=item[4], volume=int(
            item[5].split(".")[0]), note_date=item[0])
        return history
    except Exception as e:
        print(e)
        return None


def get_today_price():
    init_db()
    time_start = time.time()
    date_str = datetime.now().strftime("%Y-%m-%d")
    if(not detected(date_str)):
        print("今日未开盘")
        return -1
    # init_db()
    # rec = RedisConnection()
    # rc = rec.get_connection()
    # rc.set("lock", 1)
    # start = 0
    # thread_list = []
    # while(True):
    #     stock_list = stock_db.get_stock_list(start)
    #     print("fetch data {0} row".format(len(stock_list)))
    #     t = threading.Thread(target=do_get_today_price,
    #                          args=(stock_list, date_str))

    #     t.start()                            
    #     thread_list.append(t)
    #     start += 1000
    #     if(len(stock_list) < 1000):
    #         break

    # for t in thread_list:
    #     t.join()
    # # t_threading_list = [thread_list[i:i+3]
    # #                     for i in range(0, len(thread_list), 3)]
    # # for l in t_threading_list:
    # #     for t in l:
    # #         t.start()
    # #     for t in l:
    # #         t.join()

    # time_end = time.time()
    # time_consume = time_end - time_start
    # print("data collect done, consumer {0} second".format(time_consume))
    # stock_price_history_db.call_procedure_calculate_avg_price(date_str)
    # rc.delete("lock")

#探测请求，探测今日有无开盘
def detected(date_str:str):
    url = "https://api.doctorxiong.club/v1/stock/kline/day?token=LJUjbTGJeO&code=sh000001&startDate={0}&endDate={1}&type=1"
    full_url = url.format(date_str, date_str)
    response = requests.get(full_url)
    response_json = response.json()
    print("探测请求,{}".format(response_json))
    return response_json["code"] == 200



if __name__ == "__main__":
    get_today_price()
    # history = handle_single_stock("sh600000", "2021-11-26")
    # s = ResponseEntity.serialize(history)
    # history.set_stock_id(1)
    # now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    # history.set_create_time(now)
    # stock_price_history_db.save_dailiy_data([history])
