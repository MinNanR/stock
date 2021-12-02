from __future__ import unicode_literals, absolute_import
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String, Date, DateTime, ForeignKey, DECIMAL,UniqueConstraint
from sqlalchemy import create_engine, desc, inspect
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.serializer  import loads, dumps
import json
from flask import jsonify

ModelBase = declarative_base()


class StockInfo(ModelBase):
    __tablename__ = 'stock_info'

    id = Column(Integer, primary_key=True)
    stock_name = Column(String(length=50))
    stock_nick_code = Column(String(length=29))
    stock_code = Column(String(length=20))
    detected = Column(Integer)


class StockPriceHistory(ModelBase):
    __tablename__ = 'stock_price_history'

    id = Column(Integer, primary_key=True)
    stock_id = Column(Integer)
    start_price = Column(DECIMAL)
    end_price = Column(DECIMAL)
    highest_price = Column(DECIMAL)
    lowest_price = Column(DECIMAL)
    volume = Column(Integer)
    avg_price_past_120_days = Column(DECIMAL)
    end_price_last = Column(DECIMAL)
    avg_price_past_120_days_last = Column(DECIMAL)
    note_date = Column(Date)
    create_time = Column(DateTime)

    UniqueConstraint(stock_id, note_date, name="stock_price_history_unique_key")

def to_json(e):
    m = {}
    for field in e.__dict__:
        print(field, e.__dict__[field])

def test():
    engine = create_engine(
        "mysql+pymysql://Minnan:minnan@minnan.site:3306/stock", echo=True)
    Session = sessionmaker(bind=engine)
    session = Session()
    
    query = (session
             .query(StockPriceHistory)
             .filter(StockPriceHistory.stock_id == 1)
             .order_by(desc(StockPriceHistory.note_date))
             .limit(1))

    
    

if __name__ == "__main__":
    test()
