import datetime
from sqlalchemy import create_engine
from sqlalchemy.orm.session import Session, sessionmaker
import pandas as pd
import tushare as ts
import json
import requests
import time


datasource = {
    "host": "minnan.site",
    "port": 3306,
    "user": "Minnan",
    "password": "minnan",
    "schema": "stock"
}

host = datasource["host"]
port = datasource["port"]
user = datasource["user"]
password = datasource["password"]
schema = datasource["schema"]
url = "mysql+pymysql://{0}:{1}@{2}:{3}/{4}".format(
    user, password, host, port, schema)
engine = create_engine(url, echo=False)
Session = sessionmaker(bind=engine)

surged_up = 0.101
surged_down = 0.098
decline_up = -0.098
decline_down = -0.101

pro = ts.pro_api('bb3f904cb9cae751f3cc54468dc5ce1964490a64da56685e1c8f4ece')


def get_start_date():
    sql = "select cal_date from trade_date where cal_date <= (select max(note_date) from market_statistics) order by cal_date desc limit 130"
    data = pd.read_sql(sql, engine)
    date_list = [d.strftime("%Y%m%d") for d in data["cal_date"].to_list()]
    return date_list[len(date_list) - 1]


def get_dected_date():
    sql = "select cal_date from trade_date where cal_date > (select max(note_date) from market_statistics) and cal_date <= curdate()"
    data = pd.read_sql(sql, engine)
    date_list = [d.strftime("%Y%m%d") for d in data["cal_date"].to_list()]
    return date_list


dected_date_list = get_dected_date()


def tag_item(item):
    tag = 0
    price_differ_rate = item["pct_chg"]
    if surged_down < price_differ_rate and surged_up > price_differ_rate:
        tag = tag | (1 << 1)
    elif decline_down < price_differ_rate and decline_up > price_differ_rate:
        tag = tag | (1 << 2)
    return tag


def handle_single_stock(ts_code, start_date, end_date):
    price_list = ts.pro_bar(ts_code=ts_code, api=pro, start_date=start_date,
                            end_date=end_date, adj="qfq", freq="D", ma=[120])
    if price_list is None:
        return

    data_size = len(price_list)
    print(ts_code + ":收集到" + str(data_size) + "条数据")
    if data_size == 0:
        return

    price_list["pct_chg"] = price_list["pct_chg"] * 0.01
    price_list["tag"] = price_list.apply(lambda item: tag_item(
        item) if item["trade_date"] in dected_date_list else 0, axis=1)
    del price_list["ma_v_120"]
    data_to_insert = price_list[price_list["tag"] != 0]
    if(len(data_to_insert) > 0):
        data_to_insert.to_sql("temp_data", engine, "stock",
                              if_exists="append", index=False)
        print("成功插入数据" + data_to_insert.to_json(None,
              orient="records", date_format="yyyy-MM-dd"))


def get_market_line_data(code):
    url = "https://api.doctorxiong.club/v1/stock/kline/day?startDate=2005-01-01&type=1&code=" + code
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()["data"]
        result = {}
        for item in data:
            result[item[0]] = item[2]
        return result


def call_procedure_async_temp_data():
    session = Session()
    sql = "call async_temp_data()"
    session.execute(sql)
    session.commit()


def export_data_to_js_file():
    sql = """
    select note_date, surged_limit_count , decline_limit_count from market_statistics order by note_date
    """

    df = pd.read_sql(sql, con=engine)
    df["differ"] = df["surged_limit_count"] - df["decline_limit_count"]
    data_to_export = {}

    # data_to_export["noteDate"] = df["noteDate"].to_list()
    note_date_list = [d.strftime("%Y-%m-%d")
                      for d in df["note_date"].to_list()]
    data_to_export["noteDate"] = note_date_list
    data_to_export["surgedLineData"] = df["surged_limit_count"].to_list()
    data_to_export["declineLineData"] = df["decline_limit_count"].to_list()
    data_to_export["differ"] = df["differ"].to_list()

    sh000001 = get_market_line_data("sh000001")
    sh000016 = get_market_line_data("sh000016")
    sh000905 = get_market_line_data("sh000905")
    sz399300 = get_market_line_data("sz399300")

    data_to_export["sh000001"] = [sh000001.get(note_date)
                                  for note_date in note_date_list]
    data_to_export["sh000016"] = [sh000016.get(note_date)
                                  for note_date in note_date_list]
    data_to_export["sh000905"] = [sh000905.get(note_date)
                                  for note_date in note_date_list]
    data_to_export["sz399300"] = [sz399300.get(note_date)
                                  for note_date in note_date_list]

    # print(data_to_export)
    data = json.dumps(data_to_export)
    s = "var data = " + data
    # print(s)
    with open("data.js", "w") as f:
        f.write(s)


def get_time():
    now = datetime.datetime.now()
    return (int(now.strftime("%M")), int(now.strftime("%S")))


if __name__ == "__main__":
    stock_list = pro.stock_basic(
        exchange='', list_status='L', fields='ts_code,symbol,name,area,industry,list_date,exchange')
    stock_list = stock_list[stock_list["exchange"].isin(["SSE", "SZSE"])]

    for _, stock in stock_list.iterrows():
        print(stock["ts_code"])
        

    start_date = get_start_date()
    today = datetime.date.today()
    count = 1
    current_min = get_time()
    end_date = today.strftime("%Y%m%d")
    for _, stock in stock_list.iterrows():
        handle_single_stock(stock["ts_code"], start_date, end_date)
        count += 1
        current_time = get_time()
        if current_min[0] == current_time[0]:
            if count == 200:
                print("每分钟调取次数已达上限，程序暂停{}秒".format((60 - current_time[1])))
                time.sleep(60 - current_time[1])
                current_min = get_time()
                count = 0
        else:
            current_min = current_time
            count = 0

    call_procedure_async_temp_data()
    export_data_to_js_file()

    print("数据统计已完成")
