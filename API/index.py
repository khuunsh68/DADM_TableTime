import os
from datetime import datetime, timedelta
from functools import wraps

import jwt
import psycopg2
from flask import Flask, jsonify, request


import db

NOT_FOUND_CODE = 401
OK_CODE = 200
SUCCESS_CODE = 201
NO_CONTENT_CODE = 204
BAD_REQUEST_CODE = 400
UNAUTHORIZED_CODE = 401
FORBIDDEN_CODE = 403
NOT_FOUND = 404
SERVER_ERROR = 500

app = Flask(__name__)

# o nome deste ficheiro tem que ser index.py por causa do vercel.json (também podemos mudar o nome lá)

@app.route('/home/', methods = ["GET", "POST"])
def home():
    return "Welcome to API!"

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if "email" not in data or "password" not in data:
        return jsonify({"error": "invalid parameters"}), BAD_REQUEST_CODE

    user = db.login(data['email'], data["password"])

    if user is None:
        return jsonify({"error": "Check credentials"}), NOT_FOUND_CODE

    token = jwt.encode(
        {'user_id': user["id"], 'exp': datetime.utcnow() + timedelta(minutes=10)}, "XXXX", 'HS256')

    #user["token"] = token.decode('UTF-8')
    user["token"] = token
    return jsonify(user), OK_CODE

@app.route("/register", methods=['POST'])
def register():
    data = request.get_json()

    if "nome" not in data or "email" not in data or "password" not in data:
        return jsonify({"error": "invalid parameters"}), BAD_REQUEST_CODE

    if (db.user_exists(data)):
        return jsonify({"error": "user already exists"}), BAD_REQUEST_CODE

    user = db.add_user(data["nome"], data["email"], data["password"])

    return jsonify(user), SUCCESS_CODE



def auth_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "Authorization" not in request.headers:
            return jsonify({"error": "Token not provided"}), FORBIDDEN_CODE

        token = request.headers['Authorization']
        # Remove Bearer from token
        token = token.split(' ')[1]

        try:
            data = jwt.decode(token, "XXXX", algorithms=['HS256'])
        except jwt.ExpiredSignatureError:
            return jsonify({"error": "Token expirado", "expired": True}), UNAUTHORIZED_CODE
        except jwt.InvalidTokenError:
            return jsonify({"error": "Token inválido"}), FORBIDDEN_CODE

        request.user = db.get_user(data['user_id'])

        return f(*args, **kwargs)

    return decorated


@app.route('/getAllRestaurants', methods=['GET'])
@auth_required
def get_all_restaurants():
    restaurants = db.get_all_restaurants()
    return jsonify(restaurants), OK_CODE


@app.route('/get_restaurant/<string:restaurant_name>', methods=['GET'])
@auth_required
def get_restaurant(restaurant_name):
    restaurant = db.get_restaurant(restaurant_name)
    if restaurant is None:
        return jsonify({"error": "No content"}), NO_CONTENT_CODE
    return jsonify(restaurant), OK_CODE


@app.route('/reserva/get_all_reservas_from_user/<int:user_id>', methods=['GET'])
@auth_required
def get_all_reservas_from_user(user_id):
    reserva = db.get_all_reservas_from_user(user_id)
    if reserva is None:
        return jsonify({"error": "No content"}), NO_CONTENT_CODE
    return jsonify(reserva), OK_CODE

"""
@app.route('/reserva/verificardDisponibilidade/<int:restaurant_id>, <datetime:data_reserva>, <time:horario>, <int:quantidade>', methods=['GET'])
@auth_required
def verificar_disponibilidade_reserva(restaurant_id, data_reserva, horario, quantidade):
    seq_id = request.args.get("seq_id") or 1
    reserva = db.verificar_disponibilidade_reserva(restaurant_id, data_reserva, horario, quantidade, seq_id)
    if reserva is None:
        return jsonify({"disponivel": "horario disponivel"}), OK_CODE
    return jsonify(reserva), UNAUTHORIZED_CODE


# ----------------------------------------------------------------
@app.route("/matchs/addReserva", methods=['POST'])
@auth_required # antes de fazer esta operação primeiro vê se o token é válido 
def add_reserva():
    data = request.get_json()

    if "tournament" not in data or "date_match" not in data or "player1" not in data or "player2" not in data:
        return jsonify({"error": "invalid parameters"}), BAD_REQUEST_CODE

    matchs = db.add_matchs(data, request.user['id'])

    return jsonify(matchs), SUCCESS_CODE
"""

if __name__ == "__main__":
    app.run(debug=True)
