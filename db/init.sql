CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

INSERT INTO users (name, email, password) VALUES ('Admin', 'admin@example.com', '12345');


BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'todo_status') THEN
        CREATE TYPE todo_status AS ENUM ('To Do', 'In Progress', 'Done');
    END IF;
END


CREATE TABLE IF NOT EXISTS todo (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    status todo_status NOT NULL DEFAULT 'To Do',
    description TEXT,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO todo (title, status, description, date)
VALUES (
    'Configurar servidor de producción',
    'In Progress',
    'Debemos configurar el entorno de producción para la nueva release',
    NOW()
);
