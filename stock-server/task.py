from datetime import datetime
import requests

from requests.api import request
from mysql import stock_db, stock_price_history_db
from redis_connection import redis_connection
import json
import threading
import time

from entity import StockPriceHistory


def init_data_to_process(date_str: str):
    start = 0
    thread_list = []
    while(True):
        stock_list = stock_db.get_stock_list(start)
        # m = {"id": stock.id, "stock_nick_code": stock.stockNickCode}
        # json_string = json.dumps(m)
        # redis_connection.rpush("stock_list" + date_str, json_string)
        t = threading.Thread(target=do_get_today_price,
                             args=(stock_list, date_str))
        thread_list.append(t)
        t.start()

        start = start+1000
        if(len(stock_list) < 1000):
            break

    # redis_connection.delete("finished")
    for t in thread_list:
        t.join()


def do_get_today_price(stock_list: list, date_str: str):
    history_list = []
    for stock in stock_list:
        stock_code = stock.stockNickCode
        history = handle_single_stock(stock_code, date_str)
        if history != None:
            history_list.append(history)
    stock_price_history_db.save_dailiy_data(history_list)


def handle_single_stock(stock_code: str, date_str: str):
    url = "https://api.doctorxiong.club/v1/stock/kline/day?token=LJUjbTGJeO&code={0}&startDate={1}&endDate={2}&type=1"
    full_url = url.format(stock_code, date_str, date_str)
    response = requests.get(full_url)
    response_json = response.json()
    if(response_json["code"] != 200):
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


def get_today_price():
    date_str = datetime.now().strftime("%Y-%m-%d")
    init_data_to_process(date_str)
