CREATE TABLE IF NOT EXISTS users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       description TEXT NOT NULL,
                       available BOOLEAN,
                       owner_id BIGINT NOT NULL,
                       CONSTRAINT fk_item_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS bookings (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          start_time TIMESTAMP,
                          end_time TIMESTAMP,
                          item_id BIGINT NOT NULL,
                          booker_id BIGINT NOT NULL,
                          status VARCHAR(255) NOT NULL,
                          CONSTRAINT fk_booking_item FOREIGN KEY (item_id) REFERENCES items (id),
                          CONSTRAINT fk_booking_booker FOREIGN KEY (booker_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comments (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          text TEXT NOT NULL,
                          user_id BIGINT NOT NULL,
                          item_id BIGINT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_comment_author FOREIGN KEY (user_id) REFERENCES users (id),
                          CONSTRAINT fk_comment_item FOREIGN KEY (item_id) REFERENCES items (id)
);