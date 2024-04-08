from flask import Flask, request, jsonify
import psycopg2

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