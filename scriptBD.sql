CREATE TABLE Utilizador (
	id SERIAL PRIMARY KEY,
	nome VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL,
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
	capacidade_maxima INT
);

CREATE TABLE Reserva (
	id SERIAL PRIMARY KEY,
	id_utilizador INT REFERENCES Utilizador(id),
	id_restaurante INT REFERENCES Restaurante(id),
	data_reserva DATE NOT NULL,
	horario TIME NOT NULL,
	quantidade INT NOT NULL
);