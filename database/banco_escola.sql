-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 25/09/2026 às 00:04
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `banco_escola`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `aluno`
--

CREATE TABLE `aluno` (
  `id_aluno` int(11) NOT NULL,
  `matricula_registro` varchar(20) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `cpf` varchar(14) NOT NULL,
  `data_nascimento` date NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `telefone` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `aluno`
--

INSERT INTO `aluno` (`id_aluno`, `matricula_registro`, `nome`, `cpf`, `data_nascimento`, `email`, `telefone`) VALUES
(29, '2023abc', 'joão da silva', '123', '2003-05-12', 'joaosilva@x', '99999'),
(30, 'AL002', 'maria eduarda', '456-abc', '1990-01-01', NULL, '11 1111-1111'),
(31, '003', 'pedro', '000', '2010-12-31', 'pedro@', NULL),
(32, '2026123', 'ana clara souza', '1231231', '1985-07-20', 'ana@teste', '999674gf'),
(33, '00005', 'lucas', '999zzz', '2000-02-29', NULL, '5555-5555x'),
(34, '2026762', 'rafael', '11122233345', '2005-07-06', 'rafael@gmail', '47999998888'),
(58, '2026010001', 'Bruno Alves Costa', '38421590760', '1999-03-15', 'bruno@mail', '11999990001'),
(59, '2026010002', 'camila dos santos', '29450187633', '2004-11-02', NULL, '21999990002'),
(60, '2026010003', 'Diego Fernandes', '50187294366', '1995-07-30', 'diego@mail', NULL),
(61, '2026010004', 'elaine souza', '41029385671', '2008-01-20', 'elaine@mail', '85999990004'),
(62, '2026010005', 'Felipe Augusto Lima', '63820147590', '1992-09-09', NULL, '41999990005');

-- --------------------------------------------------------

--
-- Estrutura para tabela `avaliacao`
--

CREATE TABLE `avaliacao` (
  `id_avaliacao` int(11) NOT NULL,
  `id_turma_disciplina` int(11) NOT NULL,
  `titulo` varchar(100) NOT NULL,
  `peso` decimal(3,2) NOT NULL DEFAULT 1.00,
  `nota_maxima` decimal(4,2) NOT NULL DEFAULT 10.00,
  `data_realizacao` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `avaliacao`
--

INSERT INTO `avaliacao` (`id_avaliacao`, `id_turma_disciplina`, `titulo`, `peso`, `nota_maxima`, `data_realizacao`) VALUES
(6, 5, 'prova 1', 1.00, 10.00, '2026-03-10'),
(7, 5, 'trabalho', 0.50, 10.00, '2026-04-01'),
(8, 6, 'redação', 1.00, 10.00, '2026-03-15'),
(9, 7, 'prova única', 1.00, 10.00, '2026-03-20'),
(10, 7, 'prova2', 1.00, 10.00, '2026-09-19'),
(11, 8, 'p1', 1.00, 10.00, '2026-09-20'),
(12, 8, 'p2', 1.00, 10.00, '2026-09-22'),
(13, 8, 'p3', 1.00, 10.00, '2026-09-23'),
(15, 11, 'prova hist', 1.00, 10.00, '2026-09-21'),
(26, 22, 'avaliação 1', 1.00, 10.00, '2026-03-05'),
(27, 23, 'avaliação 1', 1.00, 10.00, '2026-08-10'),
(28, 24, 'avaliação 1', 1.00, 10.00, '2025-09-12'),
(29, 25, 'avaliação 1', 1.00, 10.00, '2026-04-18'),
(30, 26, 'avaliação 1', 1.00, 10.00, '2027-02-15');

-- --------------------------------------------------------

--
-- Estrutura para tabela `disciplina`
--

CREATE TABLE `disciplina` (
  `id_disciplina` int(11) NOT NULL,
  `codigo` varchar(20) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `carga_horaria` int(11) NOT NULL,
  `ementa` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `disciplina`
--

INSERT INTO `disciplina` (`id_disciplina`, `codigo`, `nome`, `carga_horaria`, `ementa`) VALUES
(5, 'mat1', 'matemática básica', 60, 'números e operações'),
(6, 'port1', 'português', 40, NULL),
(7, 'hist1', 'história geral', 30, 'da pré-história ao século XX'),
(19, 'geo1', 'geografia geral', 40, 'relevo e clima'),
(20, 'ing1', 'inglês básico', 30, NULL),
(21, 'qui1', 'química geral', 60, 'tabela periódica'),
(22, 'fis1', 'física mecânica', 50, 'cinemática e dinâmica'),
(23, 'bio1', 'biologia celular', 45, 'células e tecidos');

-- --------------------------------------------------------

--
-- Estrutura para tabela `matricula`
--

CREATE TABLE `matricula` (
  `id_matricula` int(11) NOT NULL,
  `id_aluno` int(11) NOT NULL,
  `id_turma` int(11) NOT NULL,
  `data_matricula` date NOT NULL DEFAULT curdate(),
  `status` varchar(20) NOT NULL DEFAULT 'ATIVA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `matricula`
--

INSERT INTO `matricula` (`id_matricula`, `id_aluno`, `id_turma`, `data_matricula`, `status`) VALUES
(8, 29, 4, '2026-09-19', 'ativa'),
(9, 30, 4, '2026-09-19', 'ativa'),
(10, 31, 4, '2026-09-19', 'ativa'),
(11, 32, 5, '2026-09-19', 'ativa'),
(12, 33, 5, '2026-09-19', 'ativa'),
(13, 34, 6, '2026-09-19', 'ATIVA'),
(29, 58, 19, '2026-02-01', 'ativa'),
(30, 59, 20, '2026-02-01', 'ativa'),
(31, 60, 21, '2025-08-01', 'ativa'),
(32, 61, 22, '2026-02-01', 'ativa'),
(33, 62, 23, '2027-02-01', 'ativa');

-- --------------------------------------------------------

--
-- Estrutura para tabela `nota`
--

CREATE TABLE `nota` (
  `id_nota` int(11) NOT NULL,
  `id_avaliacao` int(11) NOT NULL,
  `id_matricula` int(11) NOT NULL,
  `valor_obtido` decimal(4,2) NOT NULL,
  `data_lancamento` date NOT NULL DEFAULT curdate(),
  `observacao` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `nota`
--

INSERT INTO `nota` (`id_nota`, `id_avaliacao`, `id_matricula`, `valor_obtido`, `data_lancamento`, `observacao`) VALUES
(14, 6, 8, 7.50, '2026-09-19', NULL),
(15, 7, 8, 9.00, '2026-09-19', 'entregou atrasado'),
(16, 8, 8, 6.00, '2026-09-19', NULL),
(17, 6, 9, 10.00, '2026-09-19', 'nota máxima'),
(18, 7, 9, 8.00, '2026-09-19', NULL),
(19, 8, 9, 9.50, '2026-09-19', NULL),
(20, 6, 10, 4.00, '2026-09-19', 'precisa melhorar'),
(21, 7, 10, 5.50, '2026-09-19', NULL),
(22, 8, 10, 7.00, '2026-09-19', NULL),
(23, 9, 11, 8.25, '2026-09-19', NULL),
(24, 9, 12, 6.75, '2026-09-19', 'faltou um pouco de atenção'),
(25, 11, 13, 10.00, '2026-09-19', '10 muito bom!!!'),
(26, 12, 13, 9.00, '2026-09-19', 'pode melhorar'),
(27, 13, 13, 8.00, '2026-09-19', 'decepcao'),
(28, 11, 11, 9.00, '2026-09-19', ''),
(30, 15, 13, 10.00, '2026-09-19', 'muito bem rafinha'),
(41, 26, 29, 7.00, '2026-09-19', NULL),
(42, 27, 30, 8.50, '2026-09-19', NULL),
(43, 28, 31, 5.00, '2026-09-19', 'precisa refazer'),
(44, 29, 32, 9.25, '2026-09-19', 'otimo desempenho'),
(45, 30, 33, 6.60, '2026-09-19', NULL);

-- --------------------------------------------------------

--
-- Estrutura para tabela `professor`
--

CREATE TABLE `professor` (
  `id_professor` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `cpf` varchar(14) NOT NULL,
  `email` varchar(100) NOT NULL,
  `formacao` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `professor`
--

INSERT INTO `professor` (`id_professor`, `nome`, `cpf`, `email`, `formacao`) VALUES
(5, 'carlos eduardo', 'p001', 'carlos@escola', 'matemática'),
(6, 'fernanda lima', 'p002', 'fernanda@escola', 'biologia'),
(7, 'joão pereira', 'p003', 'joaop@escola', 'letras'),
(8, 'jefersson', '13123156', 'jeff@gmail.com', 'matematica'),
(21, 'Gabriela Martins', '78234501966', 'gabriela@escola', 'geografia'),
(22, 'henrique oliveira', '84512093677', 'henrique@escola', NULL),
(23, 'Isabela Cardoso', '92014837655', 'isabela@escola', 'química'),
(24, 'joão victor pereira', '15903824766', 'joaov@escola', 'física'),
(25, 'Karen Rocha', '40928175633', 'karen@escola', 'biologia');

-- --------------------------------------------------------

--
-- Estrutura para tabela `turma`
--

CREATE TABLE `turma` (
  `id_turma` int(11) NOT NULL,
  `sigla` varchar(20) NOT NULL,
  `ano_letivo` int(11) NOT NULL,
  `semestre` int(11) NOT NULL,
  `turno` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `turma`
--

INSERT INTO `turma` (`id_turma`, `sigla`, `ano_letivo`, `semestre`, `turno`) VALUES
(4, 'turma-a', 2026, 1, 'manhã'),
(5, 'turma-b', 2026, 1, 'noite'),
(6, 'turma-c', 2026, 1, 'tarde'),
(19, 'turma-d', 2026, 1, 'manhã'),
(20, 'turma-e', 2026, 2, 'tarde'),
(21, 'turma-f', 2025, 2, 'noite'),
(22, 'turma-g', 2026, 1, 'tarde'),
(23, 'turma-h', 2027, 1, 'manhã');

-- --------------------------------------------------------

--
-- Estrutura para tabela `turma_disciplina`
--

CREATE TABLE `turma_disciplina` (
  `id_turma_disciplina` int(11) NOT NULL,
  `id_turma` int(11) NOT NULL,
  `id_disciplina` int(11) NOT NULL,
  `id_professor` int(11) NOT NULL,
  `sala_aula` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `turma_disciplina`
--

INSERT INTO `turma_disciplina` (`id_turma_disciplina`, `id_turma`, `id_disciplina`, `id_professor`, `sala_aula`) VALUES
(5, 4, 5, 5, 'sala 1'),
(6, 4, 6, 6, NULL),
(7, 5, 7, 7, 'sala 2'),
(8, 6, 5, 8, 'f101'),
(11, 6, 7, 6, '512'),
(22, 19, 19, 21, 'sala 21'),
(23, 20, 20, 22, 'sala 22'),
(24, 21, 21, 23, 'sala 23'),
(25, 22, 22, 24, 'sala 24'),
(26, 23, 23, 25, 'sala 25');

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_autocomplete_aluno`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_autocomplete_aluno` (
`id` int(11)
,`label` varchar(123)
);

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_autocomplete_avaliacao`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_autocomplete_avaliacao` (
`id` int(11)
,`label` varchar(329)
);

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_autocomplete_matricula`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_autocomplete_matricula` (
`id` int(11)
,`label` varchar(123)
);

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_autocomplete_turma`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_autocomplete_turma` (
`id` int(11)
,`label` varchar(69)
);

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_autocomplete_vinculo_disciplina`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_autocomplete_vinculo_disciplina` (
`id` int(11)
,`label` varchar(226)
);

-- --------------------------------------------------------

--
-- Estrutura para view `vw_autocomplete_aluno`
--
DROP TABLE IF EXISTS `vw_autocomplete_aluno`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_autocomplete_aluno`  AS SELECT `aluno`.`id_aluno` AS `id`, concat(`aluno`.`matricula_registro`,' - ',`aluno`.`nome`) AS `label` FROM `aluno` ;

-- --------------------------------------------------------

--
-- Estrutura para view `vw_autocomplete_avaliacao`
--
DROP TABLE IF EXISTS `vw_autocomplete_avaliacao`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_autocomplete_avaliacao`  AS SELECT `av`.`id_avaliacao` AS `id`, concat(`av`.`titulo`,' - ',`d`.`nome`,' - ',`t`.`sigla`,' - ',`p`.`nome`) AS `label` FROM ((((`avaliacao` `av` join `turma_disciplina` `td` on(`av`.`id_turma_disciplina` = `td`.`id_turma_disciplina`)) join `disciplina` `d` on(`td`.`id_disciplina` = `d`.`id_disciplina`)) join `turma` `t` on(`td`.`id_turma` = `t`.`id_turma`)) join `professor` `p` on(`td`.`id_professor` = `p`.`id_professor`)) ;

-- --------------------------------------------------------

--
-- Estrutura para view `vw_autocomplete_matricula`
--
DROP TABLE IF EXISTS `vw_autocomplete_matricula`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_autocomplete_matricula`  AS SELECT `m`.`id_matricula` AS `id`, concat(`a`.`nome`,' - ',`t`.`sigla`) AS `label` FROM ((`matricula` `m` join `aluno` `a` on(`m`.`id_aluno` = `a`.`id_aluno`)) join `turma` `t` on(`m`.`id_turma` = `t`.`id_turma`)) WHERE `m`.`status` = 'ATIVA' ;

-- --------------------------------------------------------

--
-- Estrutura para view `vw_autocomplete_turma`
--
DROP TABLE IF EXISTS `vw_autocomplete_turma`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_autocomplete_turma`  AS SELECT `turma`.`id_turma` AS `id`, concat(`turma`.`sigla`,' - ',`turma`.`ano_letivo`,'/',`turma`.`semestre`,' - ',`turma`.`turno`) AS `label` FROM `turma` ;

-- --------------------------------------------------------

--
-- Estrutura para view `vw_autocomplete_vinculo_disciplina`
--
DROP TABLE IF EXISTS `vw_autocomplete_vinculo_disciplina`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_autocomplete_vinculo_disciplina`  AS SELECT `td`.`id_turma_disciplina` AS `id`, concat(`d`.`nome`,' - ',`p`.`nome`,' - ',`t`.`sigla`) AS `label` FROM (((`turma_disciplina` `td` join `disciplina` `d` on(`td`.`id_disciplina` = `d`.`id_disciplina`)) join `professor` `p` on(`td`.`id_professor` = `p`.`id_professor`)) join `turma` `t` on(`td`.`id_turma` = `t`.`id_turma`)) ;

--
-- Índices para tabelas despejadas
--

--
-- Índices de tabela `aluno`
--
ALTER TABLE `aluno`
  ADD PRIMARY KEY (`id_aluno`),
  ADD UNIQUE KEY `matricula_registro` (`matricula_registro`),
  ADD UNIQUE KEY `cpf` (`cpf`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_aluno_nome` (`nome`);

--
-- Índices de tabela `avaliacao`
--
ALTER TABLE `avaliacao`
  ADD PRIMARY KEY (`id_avaliacao`),
  ADD KEY `fk_avaliacao_turma_disciplina` (`id_turma_disciplina`),
  ADD KEY `idx_avaliacao_titulo` (`titulo`);

--
-- Índices de tabela `disciplina`
--
ALTER TABLE `disciplina`
  ADD PRIMARY KEY (`id_disciplina`),
  ADD UNIQUE KEY `codigo` (`codigo`),
  ADD KEY `idx_disciplina_nome` (`nome`);

--
-- Índices de tabela `matricula`
--
ALTER TABLE `matricula`
  ADD PRIMARY KEY (`id_matricula`),
  ADD UNIQUE KEY `unq_aluno_turma` (`id_aluno`,`id_turma`),
  ADD KEY `fk_matricula_turma` (`id_turma`);

--
-- Índices de tabela `nota`
--
ALTER TABLE `nota`
  ADD PRIMARY KEY (`id_nota`),
  ADD UNIQUE KEY `unq_avaliacao_matricula` (`id_avaliacao`,`id_matricula`),
  ADD KEY `fk_nota_matricula` (`id_matricula`);

--
-- Índices de tabela `professor`
--
ALTER TABLE `professor`
  ADD PRIMARY KEY (`id_professor`),
  ADD UNIQUE KEY `cpf` (`cpf`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_professor_nome` (`nome`);

--
-- Índices de tabela `turma`
--
ALTER TABLE `turma`
  ADD PRIMARY KEY (`id_turma`),
  ADD UNIQUE KEY `sigla` (`sigla`);

--
-- Índices de tabela `turma_disciplina`
--
ALTER TABLE `turma_disciplina`
  ADD PRIMARY KEY (`id_turma_disciplina`),
  ADD UNIQUE KEY `unq_turma_disciplina` (`id_turma`,`id_disciplina`),
  ADD KEY `fk_td_disciplina` (`id_disciplina`),
  ADD KEY `fk_td_professor` (`id_professor`);

--
-- AUTO_INCREMENT para tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `aluno`
--
ALTER TABLE `aluno`
  MODIFY `id_aluno` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=63;

--
-- AUTO_INCREMENT de tabela `avaliacao`
--
ALTER TABLE `avaliacao`
  MODIFY `id_avaliacao` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT de tabela `disciplina`
--
ALTER TABLE `disciplina`
  MODIFY `id_disciplina` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de tabela `matricula`
--
ALTER TABLE `matricula`
  MODIFY `id_matricula` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT de tabela `nota`
--
ALTER TABLE `nota`
  MODIFY `id_nota` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT de tabela `professor`
--
ALTER TABLE `professor`
  MODIFY `id_professor` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT de tabela `turma`
--
ALTER TABLE `turma`
  MODIFY `id_turma` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de tabela `turma_disciplina`
--
ALTER TABLE `turma_disciplina`
  MODIFY `id_turma_disciplina` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `avaliacao`
--
ALTER TABLE `avaliacao`
  ADD CONSTRAINT `fk_avaliacao_turma_disciplina` FOREIGN KEY (`id_turma_disciplina`) REFERENCES `turma_disciplina` (`id_turma_disciplina`) ON DELETE CASCADE;

--
-- Restrições para tabelas `matricula`
--
ALTER TABLE `matricula`
  ADD CONSTRAINT `fk_matricula_aluno` FOREIGN KEY (`id_aluno`) REFERENCES `aluno` (`id_aluno`),
  ADD CONSTRAINT `fk_matricula_turma` FOREIGN KEY (`id_turma`) REFERENCES `turma` (`id_turma`);

--
-- Restrições para tabelas `nota`
--
ALTER TABLE `nota`
  ADD CONSTRAINT `fk_nota_avaliacao` FOREIGN KEY (`id_avaliacao`) REFERENCES `avaliacao` (`id_avaliacao`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_nota_matricula` FOREIGN KEY (`id_matricula`) REFERENCES `matricula` (`id_matricula`) ON DELETE CASCADE;

--
-- Restrições para tabelas `turma_disciplina`
--
ALTER TABLE `turma_disciplina`
  ADD CONSTRAINT `fk_td_disciplina` FOREIGN KEY (`id_disciplina`) REFERENCES `disciplina` (`id_disciplina`),
  ADD CONSTRAINT `fk_td_professor` FOREIGN KEY (`id_professor`) REFERENCES `professor` (`id_professor`),
  ADD CONSTRAINT `fk_td_turma` FOREIGN KEY (`id_turma`) REFERENCES `turma` (`id_turma`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
