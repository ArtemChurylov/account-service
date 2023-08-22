CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    balance NUMERIC(12, 4) NOT NULL,
    create_date TIMESTAMP NOT NULL,
    modify_date TIMESTAMP NOT NULL
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    amount NUMERIC(12, 4) NOT NULL,
    sender INTEGER NOT NULL,
    receiver INTEGER NOT NULL,
    date TIMESTAMP NOT NULL
);
