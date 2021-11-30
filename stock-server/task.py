from datetime import datetime
import requests

from requests.api import request
from mysql import stock_db, stock_price_history_db
from redis_connection import redis_connection
import json
import threading
import time

from entity import ResponseEntity, StockPriceHistory


def init_data_to_process(date_str: str):
    start = 0
    thread_list = []
    while(True):
        stock_list = stock_db.get_stock_list(start)
        print("fetch data {0} row".format(len(stock_list)))
        t = threading.Thread(target=do_get_today_price,
                             args=(stock_list, date_str))
        thread_list.append(t)
        start += 1000
        if(len(stock_list) < 1000):
            break

    t_threading_list = [thread_list[i:i+3]
                        for i in range(0, len(thread_list), 3)]
    for l in t_threading_list:
        for t in l:
            t.start()
        for t in l:
            t.join()

    stock_price_history_db.call_procedure_calculate_avg_price(date_str)


def do_get_today_price(stock_list: list, date_str: str):
    history_list = []
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    for stock in stock_list:
        stock_code = stock.stockNickCode
        history = handle_single_stock(stock_code, date_str)
        if history != None:
            history.set_stock_id(stock.id)
            history.set_create_time(now)
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
        history = StockPriceHistory()
        history.set_start_price(item[1])
        history.set_end_price(item[2])
        history.set_highest_price(item[3])
        history.set_lowest_price(item[4])
        history.set_volume(int(item[5].split(".")[0]))
        history.set_note_date(item[0])
        return history
    except Exception as e:
        print(e)
        return None


def get_today_price():
    date_str = datetime.now().strftime("%Y-%m-%d")
    init_data_to_process(date_str)


if __name__ == "__main__":
    get_today_price()
    # history = handle_single_stock("sh600000", "2021-11-26")
    # s = ResponseEntity.serialize(history)
    # history.set_stock_id(1)
    # now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    # history.set_create_time(now)
    # stock_price_history_db.save_dailiy_data([history])
