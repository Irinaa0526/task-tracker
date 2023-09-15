ALTER SEQUENCE project_id_seq RESTART WITH 1;
UPDATE project SET project_id=nextval('project_id_seq');

ALTER SEQUENCE task_state_id_seq RESTART WITH 1;
UPDATE task_state SET task_state_id=nextval('task_state_id_seq');

ALTER SEQUENCE task_id_seq RESTART WITH 1;
UPDATE task SET task_id=nextval('task_id_seq');

INSERT INTO project (name, created_at, updated_at)
VALUES
('Лабораторная #1', now(), now()),
('Подготовка к контрольной', now(), now()),
('Дела по дому', now(), now());

INSERT INTO task_state (name, created_at, left_task_state_id, right_task_state_id, project_id)
VALUES
('Нужно сделать', now(), null, 2, 1),
('Уже сделано', now(), 1, null, 1),
('Прочитать', now(), null, 4, 2),
('Выучить', now(), 3, 5, 2),
('Запомнить', now(), 4, 6, 2),
('Повторить', now(), 5, 7, 2),
('Готово', now(), 6, null, 2),
('Нужно сделать, но никогда не сделаю', now(), null, null, 3);

INSERT INTO task (name, created_at, updated_at, description, task_state_id)
VALUES
('Сделать задание', now(), now(), '', 2),
('Написать отчет', now(), now(), '', 1),
('Помыть посуду', now(), now(), '', 8),
('Пропылесосить', now(), now(), '', 8),
('Протереть пыль', now(), now(), '', 8),
('Постирать', now(), now(), '', 8),
('Приготовить ужин', now(), now(), '', 8),
('Тема Начало правления Ивана IV', now(), now(), '', 7),
('Тема Усиление власти Ивана Грозного', now(), now(), '', 7),
('Тема Россия в конце правления Ивана IV', now(), now(), '', 7),
('Тема Смута в России', now(), now(), '', 6),
('Тема Окончание Смутного времени', now(), now(), '', 6),
('Тема Государство первых Романовых', now(), now(), '', 5),
('Тема Внешняя политика России в 17 веке', now(), now(), '', 4),
('Тема Соляной и медный бунт, восстание Степана Разина', now(), now(), '', 4),
('Тема Внешняя политика Петра I', now(), now(), '', 4),
('Тема Реформы Петра I', now(), now(), '', 4),
('Тема Дворцовые перевороты', now(), now(), '', 3);