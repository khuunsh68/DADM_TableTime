#from flask import Flask, request, jsonify
#import psycopg2

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

@app.route('/', methods = ["GET"])
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
        {'user_id': user['id'], 'exp': datetime.utcnow() + timedelta(minutes=5)}, app.config['SECRET_KEY'], 'HS256')

    user["token"] = token.decode('UTF-8')
    #user["token"] = token
    return jsonify(user), OK_CODE

@app.route("/register", methods=['POST'])
def register():
    data = request.get_json()

    if "nome" not in data or "email" not in data or "password" not in data:
        return jsonify({"error": "invalid parameters"}), BAD_REQUEST_CODE

    if (db.user_exists(data)):
        return jsonify({"error": "user already exists"}), BAD_REQUEST_CODE

    user = db.add_user(data)

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
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=['HS256'])
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


@app.route('/restaurant/<string:restaurant_name>', methods=['GET'])
@auth_required
def get_restaurant(restaurant_name):
    seq_id = request.args.get("seq_id") or 1
    restaurant = db.get_restaurant(restaurant_name, seq_id)
    if restaurant is None:
        return jsonify({"error": "No content"}), NO_CONTENT_CODE
    return jsonify(restaurant), OK_CODE


@app.route('/reserva/<int:user_id>', methods=['GET'])
@auth_required
def get_all_reservas_from_user(user_id):
    seq_id = request.args.get("seq_id") or 1
    reserva = db.get_all_reservas_from_user(user_id, seq_id)
    if reserva is None:
        return jsonify({"error": "No content"}), NO_CONTENT_CODE
    return jsonify(reserva), OK_CODE


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
















@app.route('/createUtilizador/', methods=['POST'])
def createUtilizador():
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    cur = conn.cursor()
    try:
        json = request.get_json()
        print(json)
        query = "INSERT INTO Utilizador VALUES (%s, %s, %s);"
        cur.execute(query, (json["nome"], json["email"], json["palavra_passe"]))
        conn.commit()
        return "OK", 200
    
    except Exception as e:
        d = {
            "mensagem": str(e)
        }
        return jsonify(d), 500
    
    finally:
        cur.close()
        conn.close()

@app.route('/login', methods=['POST'])
def login():
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    cur = conn.cursor()
    #verificar as credenciais fornecidas
    #gerar um token de autenticação
    #retornar o token de autenticação

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    if "username" not in data or "password" not in data:
        return jsonify({"error": "invalid parameters"}), BAD_REQUEST_CODE

    user = db.login(data['username'], data["password"])

    if user is None:
        return jsonify({"error": "Check credentials"}), NOT_FOUND_CODE

    token = jwt.encode(
        {'user_id': user['id'], 'exp': datetime.utcnow() + timedelta(minutes=5)}, app.config['SECRET_KEY'], 'HS256')

    user["token"] = token.decode('UTF-8')
    #user["token"] = token
    return jsonify(user), OK_CODE


@app.route('/getUtilizador/<varchar:email>', methods=['GET'])
def getUtilizador(email):
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    query = "SELECT * FROM Utilizador WHERE email=%s"
    cur = conn.cursor()
    cur.execute(query, [email])
    
    emps = []
    for emp_tuple in cur.fetchall():
        emp = {
            "nome": emp_tuple[0],
            "email": emp_tuple[1],
            "palavra_passe": emp_tuple[2],
            "data_registo": emp_tuple[3]
        }
        emps.append(emp)
    
    cur.close()
    conn.close()
    return jsonify(emps), 200

@app.route('/listRestaurantes/', methods=['GET'])
def listRestaurantes():
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    query = "SELECT * FROM Restaurante"
    cur = conn.cursor()
    cur.execute(query)
    
    emps = []
    for emp_tuple in cur.fetchall():
        emp = {
            "nome": emp_tuple[0],
            "endereco": emp_tuple[1],
            "horario_abertura": emp_tuple[2],
            "horario_encerramento": emp_tuple[3],
            "avaliacao": emp_tuple[4],
            "imagem_url": emp_tuple[5]
        }
        emps.append(emp)
    
    cur.close()
    conn.close()
    return jsonify(emps), 200

@app.route('/createReserva/', methods=['POST']) #falta dizer qual o utilizador e restaurante
def createReserva():
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    cur = conn.cursor()
    try:
        json = request.get_json()
        print(json)
        query = "INSERT INTO Reserva VALUES (%s, %s, %s);"
        cur.execute(query, (json["data_reserva"], json["horario"], json["quantidade"]))
        conn.commit()
        return "OK", 200
    
    except Exception as e:
        d = {
            "mensagem": str(e)
        }
        return jsonify(d), 500
    
    finally:
        cur.close()
        conn.close()

@app.route('/deleteReserva/<int:id>', methods=['DELETE'])
def deleteReserva(id):
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    cur = conn.cursor()
    query = "DELETE FROM Reserva WHERE id=%s;"
    cur.execute(query, [id])
    conn.commit()
    cur.close()
    conn.close()
    return "OK", 404

@app.route('/listReserva/<int:id_utilizador>', methods=['GET'])
def listReserva(id_utilizador):
    conn = psycopg2.connect("host=aid.estgoh.ipc.pt dbname=db109180113331 user=a109180113331 password=grupo3_dadm2024")
    query = "SELECT * FROM Reserva WHERE id_utilizador=%s"
    cur = conn.cursor()
    cur.execute(query, [id_utilizador])
    
    emps = []
    for emp_tuple in cur.fetchall():
        emp = {
            "id_utilizador": emp_tuple[0],
            "id_restaurante": emp_tuple[1],
            "data_reserva": emp_tuple[2],
            "horario": emp_tuple[3],
            "quantidade": emp_tuple[4]
        }
        emps.append(emp)
    
    cur.close()
    conn.close()
    return jsonify(emps), 200