import os
from datetime import datetime, timedelta
from re import I

import psycopg2

hostname = os.environ.get("DB_HOST")
database_name = os.environ.get("DB_DATABASE")
username = os.environ.get("DB_USER")
palavra_passe = os.environ.get("DB_PASSWORD")


def get_connection():
    return psycopg2.connect(host=hostname, database=database_name, user=username, password=palavra_passe)

def user_exists(user):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT COUNT(*) FROM utilizador WHERE email = %s", [user["email"]])
                count = cur.fetchone()[0]
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return count > 0


def login(email, password):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM utilizador WHERE email = %s AND password = crypt(%s, password)", [email, password])
                user_tuple = cur.fetchone()
                user = None
                if user_tuple is None:
                    return None
                user = {
                    "id": user_tuple[0],
                    "nome": user_tuple[1],
                    "email": user_tuple[2]
                }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return user

def get_user(user_id):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM utilizador WHERE id = %s", [user_id])
                user_tuple = cur.fetchone()
                if user_tuple is None:
                    return None
                user = None
                user = {
                        "id": user_tuple[0],
                        "email": user_tuple[1],
                        "nome": user_tuple[2]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return user

def add_user(nome, email, password):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                query = "INSERT INTO utilizador (nome, email, password) VALUES (%s, %s, crypt(%s, gen_salt('bf'))) RETURNING *"
                cur.execute(query, [nome, email, password])
                conn.commit()
                user_tuple = cur.fetchone()
                user = {
                        "id": user_tuple[0],
                        "nome": user_tuple[1],
                        "email": user_tuple[2]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return user


def get_all_restaurants():
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM restaurante")
                restaurants = []
                for restaurant_tuple in cur.fetchall():
                    restaurant = {
                        "id": restaurant_tuple[0],
                        "nome": restaurant_tuple[1],
                        "endereco": restaurant_tuple[2],
                        "horario_abertura": restaurant_tuple[3].strftime("%H:%M"),
                        "horario_encerramento": restaurant_tuple[4].strftime("%H:%M"),
                        "avaliacao": restaurant_tuple[5],
                        "imagem_url": restaurant_tuple[6],
                        "tipo_cozinha": restaurant_tuple[7]
                    }
                    restaurants.append(restaurant)      
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return restaurants


def get_restaurant(name_restaurant):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM restaurante WHERE nome = %s", [name_restaurant])
                restaurant_tuple = cur.fetchone()
                if restaurant_tuple is None:
                    return None
                restaurant = {
                        "id": restaurant_tuple[0],
                        "nome": restaurant_tuple[1],
                        "endereco": restaurant_tuple[2],
                        "horario_abertura": restaurant_tuple[3].strftime("%H:%M"),
                        "horario_encerramento": restaurant_tuple[4].strftime("%H:%M"),
                        "avaliacao": restaurant_tuple[5],
                        "imagem_url": restaurant_tuple[6],
                        "tipo_cozinha": restaurant_tuple[7]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return restaurant
        
        
def get_name_restaurant(id_restaurant):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT nome FROM restaurante WHERE id = %s", [id_restaurant])
                restaurant_tuple = cur.fetchone()
                if restaurant_tuple is None:
                    return None
                restaurant = {
                        "nome": restaurant_tuple[0]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return restaurant


def get_all_reservas_from_user(user_id):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM reserva WHERE id_utilizador = %s", [user_id])
                reservas = []
                for reserva_tuple in cur.fetchall():
                    reserva = {
                        "id": reserva_tuple[0],
                        "id_utilizador": reserva_tuple[1],
                        "id_restaurante": reserva_tuple[2],
                        "data_reserva": reserva_tuple[3].strftime("%d/%m/%Y"),
                        "horario": reserva_tuple[4].strftime("%H:%M"),
                        "quantidade": reserva_tuple[5]
                    }
                    reservas.append(reserva)
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return reservas


def verificar_disponibilidade_reserva(id_restaurant, data_reserva, horario, quantidade):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                query_capacity = "SELECT capacidade_maxima FROM restaurante WHERE id=%s"
                cur.execute(query_capacity, [id_restaurant])
                capacidade_tuple = cur.fetchone()
                if capacidade_tuple is None:
                    return {"error": "Restaurante não encontrado"}, 404
                capacidade_restaurante = capacidade_tuple[0]
                
                
                horario_obj = datetime.strptime(horario, '%H:%M:%S')
                
                hora_inicio = horario_obj - timedelta(hours=1)
                hora_fim = hora_inicio + timedelta(hours=1)
                
                query = """
                    SELECT SUM(quantidade) FROM reserva
                    WHERE id_restaurante=%s AND data_reserva=%s AND horario BETWEEN %s AND %s
                """
                parms = [id_restaurant, data_reserva, hora_inicio.strftime("%H:%M"), hora_fim.strftime("%H:%M")]
                cur.execute(query, parms)
                soma_quantidades_tuple = cur.fetchone()
                soma_quantidades = soma_quantidades_tuple[0] if soma_quantidades_tuple[0] is not None else 0

                if soma_quantidades + quantidade <= capacidade_restaurante:
                    return 0  # Disponível
                else:
                    return 1  # Indisponível
    except (Exception, psycopg2.Error) as error:
        print("Error while connecting to PostgreSQL", error)
        return {"error": str(error)}, 500
    finally:
        if(conn):
            cur.close()
            conn.close()


def add_reserva(id_user, id_restaurant, data_reserva, horario, quantidade):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("INSERT INTO reserva (id_utilizador, id_restaurante, data_reserva, horario, quantidade) VALUES (%s, %s, %s, %s, %s) RETURNING *", [id_user, id_restaurant, data_reserva, horario, quantidade])
                conn.commit()
                reserva_tuple = cur.fetchone()
                reserva = {
                        "id": reserva_tuple[0],
                        "id_utilizador": reserva_tuple[1],
                        "id_restaurante": reserva_tuple[2],
                        "data_reserva": reserva_tuple[3].strftime("%d/%m/%Y"),
                        "horario": reserva_tuple[4].strftime("%H:%M"),
                        "quantidade": reserva_tuple[5]
                    }
                return reserva
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()


def remove_reserva(id_user, id_restaurant, data_reserva, horario, quantidade):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("DELETE FROM reserva WHERE id_utilizador=%s AND id_restaurante=%s AND data_reserva=%s AND horario=%s AND quantidade=%s RETURNING *", [id_user, id_restaurant, data_reserva, horario, quantidade])
                conn.commit()
                rows_deleted = cur.rowcount
                if rows_deleted > 0:
                    return {"message": "Reserva apagada", "success": True}
                else:
                    return {"message": "Reserva não encontrada", "success": False}
    except (Exception, psycopg2.Error) as error:
        print("Error while connecting to PostgreSQL", error)
        return {"message": "Erro ao apagar reserva", "success": False}

