-- =========================================
-- FARMAEXPRES - COMPATIBLE STRUCTURE SCRIPT
-- Based on branch HU-AC-dev entities
-- Additional script, does not replace current init.sql
-- =========================================

-- =========================================
-- DATABASES
-- =========================================
CREATE DATABASE farmaexpres_users;
CREATE DATABASE farmaexpres_inventory;

-- =========================================
-- USERS DATABASE
-- =========================================
\connect farmaexpres_users;

-- =========================================
-- TABLE: role
-- Entity: Role -> idRole, name, description
-- =========================================
CREATE TABLE role (
    id_role SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- =========================================
-- TABLE: users
-- Entity: User -> id, name, email, password, State, role
-- Column from relation: IDrole
-- =========================================
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

-- =========================================
-- TABLE: binnacle
-- Entity: Binnacle -> id, userId, action, dateTime
-- =========================================
CREATE TABLE binnacle (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    date_time TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================
-- BASE DATA: roles
-- User requested current roles
-- =========================================
INSERT INTO role (name, description) VALUES
('ADMIN', 'Role for ADMIN and traceability'),
('AUDITOR', 'Role for AUDITOR operations'),
('FARMACEUTICO', 'Role for FARMACEUTICO operations');

-- =========================================
-- BASE DATA: Users
-- User requested current Users
-- =========================================
INSERT INTO public.users (email, state, name, password, idrole) VALUES
 ( 'leonardojv@gmail.com', 'Asset', 'Jose Leonardo Vargas', '$2a$10$PhkfSmC/7YOGJFcvennQ5Oz9Mi43ga7wrpl.r8BXwoOCfIPJAvCoC', 1 ),
 ( 'temenico5@gmail.com', 'Asset', 'Nicolas Tello', '$2a$10$JdJ5yd1.UKKaum5ssGK6Tu/SXA/SEDKBOK313eAvkeL2tyOYaObq6', 1 ),
 ( 'jerssson@gmail.com', 'Asset', 'Jersson Fabian Buitrago', '$2a$10$OrsSJt0u3dsDXtpJroCh.OTkHfJH7y5wvmKLjocJ0zsfDFKSXGge6', 2 ),
 ( 'marlon@gmail.com', 'Asset', 'Marlon Romero', '$2a$10$hVcBu5n6uuwsdRfoN.QRne5a79A5urgq6G1Ckrz0peBgKFhPmQodS', 3 );

-- =========================================
-- INVENTORY DATABASE
-- =========================================
\connect farmaexpres_inventory;

-- =========================================
-- TABLE: product
-- Entity: Product
-- id, name, code, stock, unitPrice, active,
-- minimumStock, expirationDate
-- =========================================
CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    stock INTEGER NOT NULL,
    unitprice NUMERIC(12,2) NOT NULL,
    asset BOOLEAN NOT NULL DEFAULT TRUE,
    minimumstock INTEGER NOT NULL,
    expirationdate DATE NOT NULL
);

-- =========================================
-- TABLE: motion
-- Entity: Motion
-- id, type, amount, dateTime, product
-- FK column: produc_id
-- =========================================
CREATE TABLE motion (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    amount INTEGER NOT NULL,
    date_time TIMESTAMP NOT NULL DEFAULT NOW(),
    produc_id BIGINT,

    CONSTRAINT fk_motion_product
        FOREIGN KEY (produc_id)
        REFERENCES product(id),

    CONSTRAINT chk_motion_type
        CHECK (type IN ('Entrance', 'Updated', 'Exit', 'Deleted'))
);

-- =========================================
-- SAMPLE DATA: product
-- Compatible with current entity fields
-- =========================================
INSERT INTO product ( name, code, stock, unitprice, asset, minimumstock, expirationdate) VALUES
('Acetaminophen 500mg', 'ACM-001', 100, 2500.00, TRUE, 20, '2026-12-31'),
('Ibuprofen 400mg', 'IBU-001', 80, 3200.00, TRUE, 15, '2027-06-30'),
('Loratadina 10 mg', 'LOT-001', 60, 6400.00, TRUE, 10, '2027-11-20'),
('Amoxicillin 500mg', 'AMX-001', 50, 7800.00, TRUE, 10, '2026-09-15'),
('Omeprazol 20 mg', 'OMP-001', 80, 9800.00, TRUE, 12, '2028-01-10'),
('Diclofenaco 50 mg', 'DCF-001', 70, 7600.00, TRUE, 10, '2028-02-28'),
('Vitamina C 1 g', 'VIC-001', 110, 6900.00, TRUE, 18, '2028-06-30'),
('Salbutamol Inhalador', 'SAT-001', 35, 28900.00, TRUE, 6, '2027-10-18'),
('Metformina 850 mg', 'MET-001', 90, 4500.00, TRUE, 15, '2027-12-31'),
('Simvastatina 20 mg', 'SIM-001', 75, 5200.00, TRUE, 10, '2028-03-31'),
('Losartan 50 mg', 'LOS-001', 75, 14100.00, TRUE, 12, '2028-05-12');

-- =========================================
-- SAMPLE DATA: motion
-- Initial records linked to products
-- =========================================
INSERT INTO motion (type, amount, produc_id)
SELECT 'Entrance', stock, id
FROM product;