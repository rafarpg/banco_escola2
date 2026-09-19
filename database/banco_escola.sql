-- Limpeza
DROP VIEW IF EXISTS vw_autocomplete_avaliacao;
DROP VIEW IF EXISTS vw_autocomplete_vinculo_disciplina;
DROP VIEW IF EXISTS vw_autocomplete_matricula;
DROP VIEW IF EXISTS vw_autocomplete_turma;
DROP VIEW IF EXISTS vw_autocomplete_aluno;

DROP TABLE IF EXISTS nota;
DROP TABLE IF EXISTS avaliacao;
DROP TABLE IF EXISTS turma_disciplina;
DROP TABLE IF EXISTS matricula;
DROP TABLE IF EXISTS turma;
DROP TABLE IF EXISTS disciplina;
DROP TABLE IF EXISTS professor;
DROP TABLE IF EXISTS aluno;
--Cria Tabelas
CREATE TABLE aluno (
    id_aluno INT NOT NULL AUTO_INCREMENT,
    matricula_registro VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefone VARCHAR(20),
    PRIMARY KEY (id_aluno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE professor (
    id_professor INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    formacao VARCHAR(100),
    PRIMARY KEY (id_professor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disciplina (
    id_disciplina INT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    carga_horaria INT NOT NULL,
    ementa TEXT,
    PRIMARY KEY (id_disciplina)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE turma (
    id_turma INT NOT NULL AUTO_INCREMENT,
    sigla VARCHAR(20) NOT NULL UNIQUE,
    ano_letivo INT NOT NULL,
    semestre INT NOT NULL,
    turno VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_turma)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE matricula (
    id_matricula INT NOT NULL AUTO_INCREMENT,
    id_aluno INT NOT NULL,
    id_turma INT NOT NULL,
    data_matricula DATE NOT NULL DEFAULT (CURRENT_DATE),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    PRIMARY KEY (id_matricula),
    CONSTRAINT fk_matricula_aluno FOREIGN KEY (id_aluno) REFERENCES aluno(id_aluno) ON DELETE RESTRICT,
    CONSTRAINT fk_matricula_turma FOREIGN KEY (id_turma) REFERENCES turma(id_turma) ON DELETE RESTRICT,
    CONSTRAINT unq_aluno_turma UNIQUE (id_aluno, id_turma)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE turma_disciplina (
    id_turma_disciplina INT NOT NULL AUTO_INCREMENT,
    id_turma INT NOT NULL,
    id_disciplina INT NOT NULL,
    id_professor INT NOT NULL,
    sala_aula VARCHAR(20),
    PRIMARY KEY (id_turma_disciplina),
    CONSTRAINT fk_td_turma FOREIGN KEY (id_turma) REFERENCES turma(id_turma) ON DELETE CASCADE,
    CONSTRAINT fk_td_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina(id_disciplina) ON DELETE RESTRICT,
    CONSTRAINT fk_td_professor FOREIGN KEY (id_professor) REFERENCES professor(id_professor) ON DELETE RESTRICT,
    CONSTRAINT unq_turma_disciplina UNIQUE (id_turma, id_disciplina)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE avaliacao (
    id_avaliacao INT NOT NULL AUTO_INCREMENT,
    id_turma_disciplina INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    peso DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    nota_maxima DECIMAL(4, 2) NOT NULL DEFAULT 10.00,
    data_realizacao DATE,
    PRIMARY KEY (id_avaliacao),
    CONSTRAINT fk_avaliacao_turma_disciplina FOREIGN KEY (id_turma_disciplina) REFERENCES turma_disciplina(id_turma_disciplina) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE nota (
    id_nota INT NOT NULL AUTO_INCREMENT,
    id_avaliacao INT NOT NULL,
    id_matricula INT NOT NULL,
    valor_obtido DECIMAL(4, 2) NOT NULL,
    data_lancamento DATE NOT NULL DEFAULT (CURRENT_DATE),
    observacao TEXT,
    PRIMARY KEY (id_nota),
    CONSTRAINT fk_nota_avaliacao FOREIGN KEY (id_avaliacao) REFERENCES avaliacao(id_avaliacao) ON DELETE CASCADE,
    CONSTRAINT fk_nota_matricula FOREIGN KEY (id_matricula) REFERENCES matricula(id_matricula) ON DELETE CASCADE,
    CONSTRAINT unq_avaliacao_matricula UNIQUE (id_avaliacao, id_matricula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE aluno ADD INDEX idx_aluno_nome (nome);
ALTER TABLE professor ADD INDEX idx_professor_nome (nome);
ALTER TABLE disciplina ADD INDEX idx_disciplina_nome (nome);
ALTER TABLE avaliacao ADD INDEX idx_avaliacao_titulo (titulo);

-- Views
CREATE VIEW vw_autocomplete_aluno AS
SELECT id_aluno AS id, CONCAT(matricula_registro, ' - ', nome) AS label FROM aluno;

CREATE VIEW vw_autocomplete_turma AS
SELECT id_turma AS id, CONCAT(sigla, ' - ', ano_letivo, '/', semestre, ' - ', turno) AS label FROM turma;

CREATE VIEW vw_autocomplete_matricula AS
SELECT m.id_matricula AS id, CONCAT(a.nome, ' - ', t.sigla) AS label
FROM matricula m
JOIN aluno a ON m.id_aluno = a.id_aluno
JOIN turma t ON m.id_turma = t.id_turma
WHERE m.status = 'ATIVA';

CREATE VIEW vw_autocomplete_vinculo_disciplina AS
SELECT td.id_turma_disciplina AS id, CONCAT(d.nome, ' - ', p.nome, ' - ', t.sigla) AS label
FROM turma_disciplina td
JOIN disciplina d ON td.id_disciplina = d.id_disciplina
JOIN professor p ON td.id_professor = p.id_professor
JOIN turma t ON td.id_turma = t.id_turma;

CREATE VIEW vw_autocomplete_avaliacao AS
SELECT av.id_avaliacao AS id, CONCAT(av.titulo, ' - ', d.nome, ' - ', t.sigla, ' - ', p.nome) AS label
FROM avaliacao av
JOIN turma_disciplina td ON av.id_turma_disciplina = td.id_turma_disciplina
JOIN disciplina d ON td.id_disciplina = d.id_disciplina
JOIN turma t ON td.id_turma = t.id_turma
JOIN professor p ON td.id_professor = p.id_professor;
