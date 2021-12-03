from datetime import date
import re
import numpy as np
from sqlalchemy import engine, create_engine, desc, func
from sqlalchemy.orm.session import Session, sessionmaker
from sqlalchemy.dialects.mysql import insert
from config import datasource

from entity import StockInfo, StockPriceHistory

engine
Session


def init_db():
    global engine, Session
    host = datasource["host"]
    port = datasource["port"]
    user = datasource["user"]
    password = datasource["password"]
    schema = datasource["schema"]
    url = "mysql+pymysql://{0}:{1}@{2}:{3}/{4}".format(
        user, password, host, port, schema)
    engine = create_engine(url, echo=True)
    Session = sessionmaker(bind=engine)


class StockDb:
    def inset_stock_batch(self, stock_list: list):

        session = Session()

        datas = [StockInfo(stock_name=item[0], stock_nick_code=item[1], stock_code=re.findall(
            "\d+", item[1])[0], detected=1) for item in stock_list]
        session.add_all(datas)
        session.commit()

    def get_stock_list(self, start: int):
        session = Session()
        results = (session
                   .query(StockInfo.id, StockInfo.stock_nick_code)
                   .filter(StockInfo.detected == 1)
                   .offset(start)
                   .limit(1000)).all()

        return results


class StockPriceHistoryDb:
    def insert_batch(self, stock_price_list: list):
        session = Session()

        for stock_price in stock_price_list:
            insert_stmt = insert(StockPriceHistory).values(
                stock_id=stock_price.id, start_price=stock_price.start_price, end_price=stock_price.end_price, highest_price=stock_price.highest_price,
                lowest_price=stock_price.lowest_price, volume=stock_price.volume, avg_price_past_120_days=stock_price.avg_price_past_120_days,
                end_price_last=stock_price.end_price_last, avg_price_past_120_days_last=stock_price.avg_price_past_120_days_last, note_date=stock_price.note_date,
                create_time=stock_price.create_time
            )
            on_duplicate_key_stmt = insert_stmt.on_duplicate_key_update(
                start_price=insert_stmt.inserted.start_price, end_price=insert_stmt.inserted.end_price, highest_price=insert_stmt.inserted.highest_price,
                lowest_price=insert_stmt.inserted.lowest_price, volume=insert_stmt.inserted.volume, avg_price_past_120_days=insert_stmt.inserted.avg_price_past_120_days,
                end_price_last=insert_stmt.inserted.end_price_last, avg_price_past_120_days_last=insert_stmt.inserted.avg_price_past_120_days_last, create_time=insert_stmt.inserted.create_time
            )
            session.execute(on_duplicate_key_stmt)

        session.commit()
        print("success to save history ,{0} row affected".format(
            len(stock_price_list)))

    def save_dailiy_data(self, stock_price_list: list):
        session = Session()

        for stock_price in stock_price_list:
            insert_stmt = insert(StockPriceHistory).values(
                stock_id=stock_price.stock_id, start_price=stock_price.start_price, end_price=stock_price.end_price, highest_price=stock_price.highest_price, lowest_price=stock_price.lowest_price,
                volume=stock_price.volume, note_date=stock_price.note_date, create_time=stock_price.create_time
            )
            on_duplicate_key_stmt = insert_stmt.on_duplicate_key_update(
                start_price=insert_stmt.inserted.start_price, end_price=insert_stmt.inserted.end_price, highest_price=insert_stmt.inserted.highest_price, lowest_price=insert_stmt.inserted.lowest_price,
                volume=insert_stmt.inserted.volume, create_time=insert_stmt.inserted.create_time
            )
            session.execute(on_duplicate_key_stmt)

        session.commit()
        print("success to save history ,{0} row affected".format(
            len(stock_price_list)))

    def get_eliablge_stock_list(self, note_date: str, start: int, page_size: int):

        session = Session()
        results = (session
                   .query(StockPriceHistory, StockInfo.stock_name, StockInfo.stock_code)
                   .join(StockInfo, StockInfo.id == StockPriceHistory.stock_id)
                   .filter(StockPriceHistory.note_date == note_date)
                   .offset(start)
                   .limit(page_size)).all()
        return results

    def count_eliablge_stock_list(self, note_date: str):
        session = Session()
        count = session.query(func.count(StockPriceHistory.id)).filter(
            StockPriceHistory.note_date == note_date).scalar()
        return count

    def call_procedure_calculate_avg_price(self, note_date: str):
        sql = "call calculate_avg_price('{0}')".format(note_date)
        session = Session()

        session.execute(sql)
        session.commit()


stock_db = StockDb()
stock_price_history_db = StockPriceHistoryDb()
