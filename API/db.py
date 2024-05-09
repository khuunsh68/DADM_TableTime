import os
from re import I

import psycopg2

hostname = "aid.estgoh.ipc.pt"
database_name = "db109180113331"
username = "a109180113331"
palavra_passe = "grupo3_dadm2024"


def get_connection():
    return psycopg2.connect(host=hostname, database = database_name, user=username, password = palavra_passe)

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
                    "email": user_tuple[1],
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
                user = {
                        "id": user_tuple[0],
                        "email": user_tuple[1],
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
                cur.execute("INSERT INTO utilizador (nome, email, password) VALUES (%s, %s, crypt(%s, gen_salt('bf'))) RETURNING *", [nome, email, password])
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
                restaurant_tuple = cur.fetchone()
                if restaurant_tuple is None:
                    return None
                restaurant = {
                        "id": restaurant_tuple[0],
                        "nome": restaurant_tuple[1],
                        "endereco": restaurant_tuple[2],
                        "horario_abertura": restaurant_tuple[3],
                        "horario_encerramento": restaurant_tuple[4],
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
                        "horario_abertura": restaurant_tuple[3],
                        "horario_encerramento": restaurant_tuple[4],
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


def get_all_reservas_from_user(user):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM reservas WHERE id_utilizador = %s", user["id"])
                reserva_tuple = cur.fetchone()
                if reserva_tuple is None:
                    return None
                reserva = {
                        "id": reserva_tuple[0],
                        "id_utilizador": reserva_tuple[1],
                        "id_restaurante": reserva_tuple[2],
                        "data_reserva": reserva_tuple[3],
                        "horario": reserva_tuple[4],
                        "quantidade": reserva_tuple[5]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return reserva


def verificar_disponibilidade_reserva(restaurant, data_reserva, horario, quantidade):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("SELECT * FROM reserva WHERE id_restaurante=%s AND data_reserva=%s AND horario=%s AND quantidade=%s", [restaurant["id"], data_reserva, horario, quantidade])
                reserva_tuple = cur.fetchone()
                if reserva_tuple is None:
                    return None
                reserva = {
                        "id": reserva_tuple[0],
                        "id_utilizador": reserva_tuple[1],
                        "id_restaurante": reserva_tuple[2],
                        "data_reserva": reserva_tuple[3],
                        "horario": reserva_tuple[4],
                        "quantidade": reserva_tuple[5]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return reserva


def add_reserva(user, restaurant, data_reserva, horario, quantidade):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("INSERT INTO reserva (id_utilizador, id_restaurant, data_reserva, horario, quantidade) VALUES (%s, %s, %s, %s, %s) RETURNING *", [user["id"], restaurant["id"], data_reserva, horario, quantidade])
                conn.commit()
                reserva_tuple = cur.fetchone()
                reserva = {
                        "id": reserva_tuple[0],
                        "id_utilizador": reserva_tuple[1],
                        "id_restaurante": reserva_tuple[2],
                        "data_reserva": reserva_tuple[3],
                        "horario": reserva_tuple[4],
                        "quantidade": reserva_tuple[5]
                    }
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return reserva


def remove_reserva(reserva):
    try:
        with get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute("DELETE FROM reserva WHERE id=%s RETURNING *", [reserva["id"]])
                conn.commit()
                var = None
                var = len(cur.fetchall()) > 0
    except (Exception, psycopg2.Error) as error :
        print ("Error while connecting to PostgreSQL", error)
    finally:
        if(conn):
            cur.close()
            conn.close()
        return var  

