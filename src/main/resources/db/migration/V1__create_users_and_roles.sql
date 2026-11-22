CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       name VARCHAR(100) NOT NULL,

                       email VARCHAR(255) NOT NULL,

                       password_hash VARCHAR(255) NOT NULL,

                       enabled BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_users_email
    ON users (LOWER(email));


CREATE TABLE roles (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       name VARCHAR(30) NOT NULL UNIQUE
);


CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,

                            role_id BIGINT NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE CASCADE
);


INSERT INTO roles (name)
VALUES
    ('BUYER'),
    ('SELLER'),
    ('ADMIN');