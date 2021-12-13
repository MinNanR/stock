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
        # return json.dumps(self, default=lambda obj: obj.__dict__,
        #                   sort_keys=True, ensure_ascii=False)
        return {
            "code": self.code,
            "message": self.message,
            "data": self.data
        }

    @staticmethod
    def success(data=None, message=RESPONSE_CODE_SUCCESS.message):
        response_entity = ResponseEntity(RESPONSE_CODE_SUCCESS)
        response_entity.data = data
        response_entity.message = message
        return response_entity

    @staticmethod
    def message(message: str):
        response_entity = ResponseEntity(RESPONSE_CODE_SUCCESS)
        response_entity.message = message
        return response_entity

    @staticmethod
    def fail(response_code=RESPONSE_CODE_FAIL, message=RESPONSE_CODE_FAIL.message, data=None):
        response_entity = ResponseEntity(response_code)
        response_entity.data = data
        response_entity.message = message
        return response_entity

    @staticmethod
    def invalid_param(response_code=RESPONSE_CODE_INVALID_PARAM, message=RESPONSE_CODE_INVALID_PARAM.message, data=None):
        response_entity = ResponseEntity(response_code)
        response_entity.data = data
        response_entity.message = message
        return response_entity

class ListQueryVO:
    def __init__(self, l: list, total_count: int):
        self.list = l
        self.totalCount = total_count