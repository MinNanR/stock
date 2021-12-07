from flask import request


class DTO:
    def __init__(self):
        self.m = {}


class LoginDTO(DTO):
    def username(self, username=None, isSet=False):
        if(username != None or isSet):
            self.m["username"] = username
            return self
        else:
            return self.m["username"]

    def password(self, password=None, isSet=False):
        if(password != None or isSet):
            self.m["password"] = password
            return self
        else:
            return self.m["password"]

    def validate(self):
        validat_message = []
        if self.m["username"] == None or len(self.m["username"]) == 0:
            validat_message.append({"field": "username", "message": "用户名不能为空"})
        if self.m["password"] == None or len(self.m["password"]) == 0:
            validat_message.append({"field": "password", "message": "密码不能为空"})
        return validat_message

    def __init__(self, request):
        DTO.__init__(self)
        j = request.json
        print(j)
        (self
         .username(j.get("username"), isSet=True)
         .password(j.get("password"), isSet=True)
         )
        print(self.m)

class GetEligibleStockList(DTO):
    def noteDate(self, noteDate=None, isSet=False):
        if(noteDate != None or isSet):
            self.m['noteDate'] = noteDate
            return self
        else:
            return self.m[noteDate]

    def pageSize(self, pageSize=None, isSet=False):
        if(pageSize != None or isSet):
            self.m['pageSize'] = pageSize
            return self
        else:
            return self.m[pageSize]

    def pageIndex(self, pageIndex=None, isSet=False):
        if(pageIndex != None or isSet):
            self.m['pageIndex'] = pageIndex
            return self
        else:
            return self.m[pageIndex]
            
    def get_start(self):
        return (self.pageIndex() - 1) * self.pageSize()

    def validate(self):
        validate_message = []
        if(self.m["noteDate"] == None or len(self.m["noteDate"]) == 0):
            validate_message.append({"field":"noteDate", "message":"统计日期不能为空"})
        if(self.m["pageIndex"] == None):
            validate_message.append({"field":"pageIndex", "message":"页码不能为空"})
        if(self.m["pageSize"] == None):
            validate_message.append({"field":"pageSize", "message":"每页显示数量不能为空"})
        return validate_message

    def __init__(self, request):
        DTO.__init__(self)
        j = request.json
        (self
        .noteDate(j.get("noteDate"), isSet=True)
        .pageIndex(j.get("pageIndex"), isSet=True)
        .page_size(j.get("page_size"), isSet=True)
        )