from dis import code_info
from os import openpty
from pandas import DataFrame
import tushare as ts

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
                      end_date=end_date, adj="qfq", freq="D")
    d = data.sort_values(by=["trade_date"])
    return d.to_json(None, orient="records", date_format="yyyy-MM-dd")


def dected(date):
    dected = pro.trade_cal(exchange='', start_date=date, end_date=date)
    return dected.to_json(None, orient="records", date_format="yyyy-MM-dd")


if __name__ == '__main__':
    data = ts.pro_bar(ts_code='300801.SZ', api=pro, start_date='20210101',
                      end_date='20220520', adj="qfq", freq="D", ma=[120])
    data.to_json("300801.json", orient="records", date_format="yyyy-MM-dd")