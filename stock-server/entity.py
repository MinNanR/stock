from __future__ import unicode_literals, absolute_import
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Column, Integer, String, Date, DateTime, ForeignKey, DECIMAL, UniqueConstraint
from sqlalchemy import create_engine, desc, inspect
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.serializer import loads, dumps
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

    UniqueConstraint(stock_id, note_date,
                     name="stock_price_history_unique_key")


class AuthUser(ModelBase):
    __tablename__ = "auth_user"

    id = Column(Integer, primary_key=True)
    username = Column(String(length=50))
    password = Column(String(length=200))
    password_stamp = Column(String(length=32))
    nick_name = Column(String(length=30))
    role = Column(String(length=20))
    create_time = Column(DateTime)
    create_user_id = Column(Integer)
    create_user_name = Column(String(length=30))
    update_time = Column(DateTime)
    update_user_id = Column(Integer)
    update_user_name = Column(String(length=30))
