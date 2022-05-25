from pandas import DataFrame
import tushare as ts
from sqlalchemy import engine, create_engine

pro = ts.pro_api('515a80e97276fd45f2f4c1a660c0b28a25d53b18ba3fb16893804c7a')

# data = pro.stock_basic(exchange='', list_status='L',
#                        fields='ts_code,symbol,name,area,industry,list_date')
# code = data['ts_code'][1000]
# print(code)

# df = pro.daily(ts_code=code, start_date='20220101', end_date='20220516')
# df.to_json(path_or_buf=code + ".json",
#            orient="records", date_format='yyyy-MM-dd')


def get_all_stock():
    data = pro.stock_basic(exchange='', list_status='L',
                           fields='ts_code,symbol,name,area,industry,list_date')
    return data.to_json(None, orient="records", date_format="yyyy-MM-dd")


def get_stock_price(ts_code, start_date, end_date):
    data = ts.pro_bar(ts_code=ts_code, api=pro, start_date=start_date,
                      end_date=end_date, adj="qfq", freq="D", ma=[120])
    # d = data.sort_values(by=["trade_date"])
    return data.to_json(None, orient="records", date_format="yyyy-MM-dd") if data is not None else DataFrame().to_json(None, orient="records", date_format="yyyy-MM-dd")


def dected(date):
    dected = pro.trade_cal(exchange='', start_date=date, end_date=date)
    return dected.to_json(None, orient="records", date_format="yyyy-MM-dd")


if __name__ == '__main__':
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
    data = pro.trade_cal(
        exchange='', start_date="20200101", end_date="20230101")
    data = data[data["is_open"] == 1]["cal_date"]
    data.to_sql("trade_date", engine, "stock", if_exists="append", index=False)
    print(data)
