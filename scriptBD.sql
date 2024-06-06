CREATE TABLE Utilizador (
	id SERIAL PRIMARY KEY,
	nome VARCHAR(100) NOT NULL,
	email VARCHAR(100) UNIQUE NOT NULL,
	password VARCHAR(100) NOT NULL,
	data_registo TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Restaurante (
	id SERIAL PRIMARY KEY,
	nome VARCHAR(100) NOT NULL,
	endereco VARCHAR(100),
	horario_abertura TIME NOT NULL,
	horario_encerramento TIME NOT NULL,
	avaliacao DECIMAL(2,1) CHECK (avaliacao >= 0 AND avaliacao <= 5) DEFAULT 0,
	imagem_url VARCHAR(255),
	tipo_cozinha VARCHAR(255),
	capacidade_maxima INT NOT NULL
);

CREATE TABLE Reserva (
	id SERIAL PRIMARY KEY,
	id_utilizador INT REFERENCES Utilizador(id) NOT NULL,
	id_restaurante INT REFERENCES Restaurante(id) NOT NULL,
	data_reserva DATE NOT NULL,
	horario TIME NOT NULL,
	quantidade INT NOT NULL
);



/* TRIGGERS */

-- Trigger para validar capacidade do restaurante numa hora de intervalo
CREATE OR REPLACE FUNCTION check_restaurant_capacity_one_hour() 
RETURNS TRIGGER AS $$
DECLARE
    current_reservations INT;
    restaurant_capacity INT;
BEGIN
    SELECT capacidade_maxima INTO restaurant_capacity
    FROM Restaurante
    WHERE id = NEW.id_restaurante;

    SELECT SUM(quantidade) INTO current_reservations
    FROM Reserva
    WHERE id_restaurante = NEW.id_restaurante
    AND data_reserva = NEW.data_reserva
    AND horario BETWEEN NEW.horario - INTERVAL '1 hour' AND NEW.horario + INTERVAL '1 hour';

    IF current_reservations + NEW.quantidade > restaurant_capacity THEN
        RAISE EXCEPTION 'Capacidade máxima do restaurante excedida para o intervalo de uma hora';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_capacity_one_hour
BEFORE INSERT ON Reserva
FOR EACH ROW
EXECUTE FUNCTION check_restaurant_capacity_one_hour();


-- Trigger para verificar se a reserva está dentro do horário de funcionamento do restaurante
CREATE OR REPLACE FUNCTION check_reservation_time_within_opening_hours() 
RETURNS TRIGGER AS $$
DECLARE
    opening_time TIME;
    closing_time TIME;
BEGIN
    SELECT horario_abertura, horario_encerramento INTO opening_time, closing_time
    FROM Restaurante
    WHERE id = NEW.id_restaurante;

    IF NOT (NEW.horario BETWEEN opening_time AND closing_time) THEN
        RAISE EXCEPTION 'A reserva está fora do horário de funcionamento do restaurante';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_reservation_time
BEFORE INSERT ON Reserva
FOR EACH ROW
EXECUTE FUNCTION check_reservation_time_within_opening_hours();