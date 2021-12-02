from datetime import date, datetime
import json
import re


class ResponseCode:
    def __init__(self, code: str, message: str):
        self.code = code
        self.message = message


RESPONSE_CODE_SUCCESS = ResponseCode("000", "操作成功")
RESPONSE_CODE_INVLIAD_USER = ResponseCode("002", "非法用户")
RESPONSE_CODE_FAIL = ResponseCode("001", "操作失败")
RESPONSE_CODE_INVALID_PARAM = ResponseCode("005", "参数非法")
RESPONSE_CODE_USERNAME_EXIST = ResponseCode("010", "用户名已存在")
RESPONSE_CODE_UNKNOWN_ERROR = ResponseCode("500", "未知错误")


class ResponseEntity:
    def __init__(self, response_code: ResponseCode):
        self.code = response_code.code
        self.message = response_code.message

    def serialize(self):
        return json.dumps(self, default=lambda obj: obj.__dict__,
                          sort_keys=True, ensure_ascii=False)

    @staticmethod
    def success(data: any,message=RESPONSE_CODE_SUCCESS.message):
        response_entity = ResponseEntity(RESPONSE_CODE_SUCCESS)
        response_entity.data = data
        if message != None:
            response_entity.message = message
        return response_entity

    @staticmethod
    def message(message: str):
        response_entity = ResponseEntity(RESPONSE_CODE_SUCCESS)
        response_entity.message = message
        return response_entity

    @staticmethod
    def fail(response_code: ResponseCode, message: str, data: any):
        response_entity = ResponseEntity(response_code)
        response_entity.data = data


class ListQueryVO:
    def __init__(self, l: list, total_count: int):
        self.list = l
        self.totalCount = total_count


class UserVO:
    def __init__(self, id: int, username: str):
        self.id = id
        self.username: username

# 股票实体类


class StockInfo:
    def __init__(self, id: int, stock_name: str, stock_nick_code: str, stock_code: str, detected: int):
        self.id = id
        self.stockName = stock_name
        self.stockNickCode = stock_nick_code
        if (stock_code == None):
            self.stockCode = re.findall("\d+", stock_nick_code)[0]
        else:
            self.stockCode = stock_code

        self.detected = 1 if detected == None else detected


class StockPriceHistory:
    def __init__(self):
        pass

    def set_id(self, id: int):
        self.id = id

    def set_stock_id(self, stock_id: int):
        self.stockId = stock_id

    def set_start_price(self, start_price: str):
        self.startPrice = start_price

    def set_end_price(self, end_price: str):
        self.endPrice = end_price

    def set_highest_price(self, highest_price: str):
        self.highestPrice = highest_price

    def set_lowest_price(self, lowest_price: str):
        self.lowestPrice = lowest_price

    def set_volume(self, volume: int):
        self.volume = volume

    def set_avg_price_past_120_days(self, avg_price: str):
        self.avgPricePast120Days = avg_price

    def set_end_price_last(self, end_price_last: str):
        self.endPriceLast = end_price_last

    def set_avg_price_past_120_days_last(self, avg_price: str):
        self.avgPricePast120DaysLast = avg_price

    def set_note_date(self, note_date: date):
        self.noteDate = note_date

    def set_create_time(self, create_time: datetime):
        self.createTime = create_time

    def set_stock_name(self, stock_name: str):
        self.stockName = stock_name

    def set_stock_code(self, stock_code: str):
        self.stockCode = stock_code
