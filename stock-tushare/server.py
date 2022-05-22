from flask import Flask, make_response, request
import ts

app = Flask(__name__)

@app.route('/getAllStock', methods=["POST"])
def index():
    all_stock = ts.get_all_stock()
    response = make_response(all_stock, 200)
    response.headers['Content-Type'] = "application/json"
    return response

@app.route("/getStockPriceHistory", methods=["POST"])
def get_stock_price_history():
    param = request.json
    price_data = ts.get_stock_price(param["stockNickCode"], param["startDate"], param["endDate"])
    response = make_response(price_data, 200)
    response.headers["Content-Type"] = "application/json"
    return response
    
@app.route("/dected", methods=["POST"])
def dected():
    param = request.json
    dected = ts.dected(param["date"])    
    response = make_response(dected, 200)
    response.headers["Content-Type"] = "application"
    return response
    

if __name__=='__main__':
    app.run(port=8151,debug=True)