CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    state VARCHAR(20) NOT NULL,
    idrole INTEGER NOT NULL,
    CONSTRAINT fk_users_role
        FOREIGN KEY (idrole)
        REFERENCES role(id_role),
    CONSTRAINT chk_user_state
        CHECK (state IN ('Asset', 'Idle', 'Blocked'))
);
