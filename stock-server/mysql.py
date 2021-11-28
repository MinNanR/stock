import numpy as np
import pymysql
import threading
from entity import StockInfo, StockPriceHistory

conn = pymysql.connect(host="minnan.site", port=3306,
                       user="Minnan", passwd="minnan", db="stock")
lock = threading.Lock()


class StockDb:
    def inset_stock_batch(self, stock_list: list):
        prepared_sql = """
        insert into stock_info(stock_name, stock_nick_code, stock_code, detected) VALUES (%s, %s, %s, %s)
        """

        data = []
        for stock in stock_list:
            data.append((stock.stockName, stock.stockNickCode,
                        stock.stockCode, stock.detected))

        with conn.cursor() as cursor:
            insert_count = cursor.executemany(prepared_sql, data)

        conn.commit()
        print("success to insert ,%d row affected" % insert_count)

    def get_stock_list(self, start: int):
        prepared_sql = """
        select id, stock_nick_code from stock_info 
        where id not in (select stock_id from stock_price_history where note_date ='2021-11-26')
        limit {0},{1}
        """

        result_list = []
        lock.acquire()
        with conn.cursor() as cursor:
            sql = prepared_sql.format(start, 1000)
            cursor.execute(sql)
            select_result = cursor.fetchall()
            for result_line in select_result:
                stock = StockInfo(
                    id=result_line[0], stock_name=None, stock_nick_code=result_line[1], stock_code=None, detected=None)
                result_list.append(stock)
        lock.release()

        return result_list


class StockPriceHistoryDb:
    def insert_batch(self, stock_price_list: list):
        prepared_sql = """
        insert into stock_price_history(stock_id, start_price, end_price, highest_price, lowest_price, volume, avg_price_past_120_days, end_price_last, avg_price_past_120_days_last, note_date, create_time) 
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) on duplicate key update start_price=values(start_price),end_price=values(end_price),highest_price=values(highest_price),
            lowest_price=values(lowest_price),volume=values(volume),avg_price_past_120_days=values(avg_price_past_120_days),
            avg_price_past_120_days_last=values(avg_price_past_120_days_last), create_time=values(create_time), end_price_last=values(end_price_last)
        """

        data = [(item.stockId, item.startPrice, item.endPrice, item.highestPrice, item.lowestPrice,
                 item.volume, item.avgPricePast120Days, item.endPriceLast, item.avgPricePast120DaysLast, item.noteDate, item.createTime)
                for item in stock_price_list]

        lock.acquire()
        with conn.cursor() as cursor:
            insert_count = cursor.executemany(prepared_sql, data)
        conn.commit()
        lock.release()
        print("success to save history ,%d row affected" % insert_count)

    def save_dailiy_data(self, stock_price_list: list):
        prepared_sql = """
        insert into stock_price_history(stock_id, start_price, end_price, highest_price, lowest_price, volume, note_date, create_time) 
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s) on duplicate key update start_price=values(start_price),end_price=values(end_price),highest_price=values(highest_price),
            lowest_price=values(lowest_price),volume=values(volume),create_time=values(create_time)
        """

        data = [(item.stockId, item.startPrice, item.endPrice, item.highestPrice, item.lowestPrice,
                 item.volume, item.noteDate, item.createTime)
                for item in stock_price_list]

        lock.acquire()
        with conn.cursor() as cursor:
            insert_count = cursor.executemany(prepared_sql, data)
        conn.commit()
        lock.release()
        print("success to save history ,%d row affected" % insert_count)
        
    def get_eliablge_stock_list(self, note_date: str):
        prepared_sql = """
        select t1.id, t2.id, t2.stock_name, t2.stock_code, t1.start_price, t1.end_price, t1.highest_price, t1.lowest_price,
            t1.avg_price_past_120_days, t1.create_time
        from stock_price_history t1
                left join stock_info t2 on t1.stock_id = t2.id
        where t1.note_date = %s and (t1.end_price > t1.avg_price_past_120_days and t1.end_price_last < t1.avg_price_past_120_days_last)
        """

        lock.acquire()
        data_list = []
        with conn.cursor() as cursor:
            cursor.execute(prepared_sql, (note_date,))
            result_list = cursor.fetchall()
            print(len(result_list))
            for result_line in result_list:
                history = StockPriceHistory()
                history.set_id(int(result_line[0]))
                history.set_stock_id(int(result_line[1]))
                history.set_stock_name(result_line[2])
                history.set_stock_code(float(result_line[3]))
                history.set_start_price(float(result_line[4]))
                history.set_end_price(float(result_line[5]))
                history.set_highest_price(float(result_line[6]))
                history.set_lowest_price(float(result_line[7]))
                history.set_avg_price_past_120_days(float(result_line[8]))
                history.set_create_time(
                    result_line[9].strftime("%Y-%m-%d %H:%M:%S"))
                data_list.append(history)
        lock.release()
        return data_list

    def count_eliablge_stock_list(self, note_date: str):
        prepared_sql = """
        select count(1)
        from stock_price_history t1
        where t1.note_date = %s
        and (t1.end_price > t1.avg_price_past_120_days and t1.end_price_last < t1.avg_price_past_120_days_last)
        """

        lock.acquire()
        count = 0
        with conn.cursor() as cursor:
            cursor.execute(prepared_sql, (note_date,))
            count = cursor.fetchone()[0]
        lock.release()
        return int(count)


stock_db = StockDb()
stock_price_history_db = StockPriceHistoryDb()
