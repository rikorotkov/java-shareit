CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(255),
    email VARCHAR(255) UNIQUE
    );

CREATE TABLE IF NOT EXISTS requests (
                                        id SERIAL PRIMARY KEY,
                                        description TEXT,
                                        created TIMESTAMP,
                                        author_id INTEGER REFERENCES users(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS items (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(255),
    description TEXT,
    available BOOLEAN,
    owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    item_request_id INTEGER REFERENCES requests(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS bookings (
                                        id SERIAL PRIMARY KEY,
                                        start_time TIMESTAMP,
                                        end_time TIMESTAMP,
                                        item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    booker_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS comments (
                                        id SERIAL PRIMARY KEY,
                                        text TEXT,
                                        user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );