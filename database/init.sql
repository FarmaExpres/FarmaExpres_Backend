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
    date_time TIMESTAMPTZ NOT NULL DEFAULT NOW()
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
    nombre_generico VARCHAR(200) NOT NULL,
    concentracion VARCHAR(100) NOT NULL,
    forma_farmaceutica VARCHAR(100) NOT NULL,
    presentacion VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    stock INTEGER NOT NULL,
    unitprice NUMERIC(12,2) NOT NULL,
    stock_maximo INTEGER NOT NULL,
    precio_compra NUMERIC(12,2) NOT NULL,
    precio_venta NUMERIC(12,2) NOT NULL,
    requiere_receta BOOLEAN NOT NULL,
    laboratorio VARCHAR(150),
    registro_sanitario VARCHAR(150),
    via_administracion VARCHAR(100) NOT NULL,
    unidad_medida VARCHAR(60) NOT NULL,
    ubicacion_almacen VARCHAR(150) NOT NULL,
    temperatura_conservacion VARCHAR(100) NOT NULL,
    observaciones TEXT,
    asset BOOLEAN NOT NULL DEFAULT TRUE,
    minimumstock INTEGER NOT NULL,
    expirationdate DATE NOT NULL,

    CONSTRAINT chk_product_text_required_not_blank
        CHECK (
            btrim(name) <> ''
            AND btrim(nombre_generico) <> ''
            AND btrim(concentracion) <> ''
            AND btrim(forma_farmaceutica) <> ''
            AND btrim(presentacion) <> ''
            AND btrim(via_administracion) <> ''
            AND btrim(unidad_medida) <> ''
            AND btrim(ubicacion_almacen) <> ''
            AND btrim(temperatura_conservacion) <> ''
        ),
    CONSTRAINT chk_product_forma_farmaceutica
        CHECK (forma_farmaceutica IN ('TABLETA', 'CAPSULA', 'JARABE', 'SUSPENSION', 'INYECTABLE', 'CREMA', 'GOTAS', 'AMPOLLA', 'SUPOSITORIO', 'OTRO')),
    CONSTRAINT chk_product_via_administracion
        CHECK (via_administracion IN ('ORAL', 'INTRAVENOSA', 'INTRAMUSCULAR', 'SUBCUTANEA', 'TOPICA', 'INHALATORIA', 'OFTALMICA', 'OTICA', 'NASAL', 'RECTAL', 'VAGINAL', 'OTRA')),
    CONSTRAINT chk_product_unidad_medida
        CHECK (unidad_medida IN ('UNIDAD', 'BLISTER', 'CAJA', 'FRASCO', 'VIAL', 'AMPOLLA', 'TUBO', 'SOBRE', 'JERINGA', 'UI', 'MCG', 'ML', 'MG', 'G')),
    CONSTRAINT chk_product_temperatura_conservacion
        CHECK (temperatura_conservacion IN ('AMBIENTE', 'REFRIGERADO', 'CONGELADO', 'CONTROLADA', 'NO_APLICA'))
);

-- =========================================
-- TABLE: batch
-- Entity: Batch
-- =========================================
CREATE TABLE batch (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    batch_code VARCHAR(100) NOT NULL,
    expiration_date DATE NOT NULL,
    initial_stock INTEGER NOT NULL,
    available_stock INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_batch_product
        FOREIGN KEY (product_id)
        REFERENCES product(id),

    CONSTRAINT uk_batch_product_code
        UNIQUE (product_id, batch_code),

    CONSTRAINT chk_batch_available_stock
        CHECK (available_stock >= 0),

    CONSTRAINT chk_batch_status
        CHECK (status IN ('ACTIVE', 'OUT_OF_STOCK', 'EXPIRED', 'RETIRED'))
);

-- =========================================
-- TABLE: motion
-- Entity: Motion
-- id, type, amount, dateTime, product
-- FK column: produc_id + batch_id
-- =========================================
CREATE TABLE motion (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    amount INTEGER NOT NULL,
    date_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reason VARCHAR(255),
    user_id BIGINT,
    user_name VARCHAR(150),
    user_email VARCHAR(180),
    user_role VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    marked_by_user_id BIGINT,
    marked_by_user_name VARCHAR(150),
    marked_at TIMESTAMPTZ,
    observation TEXT,
    adjustment_summary TEXT,
    adjustment_detail JSONB,
    produc_id BIGINT,
    batch_id BIGINT,

    CONSTRAINT fk_motion_product
        FOREIGN KEY (produc_id)
        REFERENCES product(id),

    CONSTRAINT fk_motion_batch
        FOREIGN KEY (batch_id)
        REFERENCES batch(id),

    CONSTRAINT chk_motion_type
        CHECK (type IN ('Entrance', 'Updated', 'Exit', 'Deleted'))
);

-- =========================================
-- SAMPLE DATA: product
-- Compatible with current entity fields
-- =========================================
INSERT INTO product (
    name, nombre_generico, concentracion, forma_farmaceutica, presentacion,
    code, stock, unitprice, stock_maximo, precio_compra, precio_venta, requiere_receta,
    laboratorio, registro_sanitario, via_administracion, unidad_medida,
    ubicacion_almacen, temperatura_conservacion, observaciones,
    asset, minimumstock, expirationdate
) VALUES
-- Activo sin alertas
('Acetaminophen 500mg', 'Acetaminofen', '500 mg', 'TABLETA', 'Caja x 20 tabletas',
 'ACM-001', 120, 2500.00, 250, 1200.00, 2500.00, FALSE,
 'Genfar', 'RS-ACM-001', 'ORAL', 'CAJA',
 'Bodega A1', 'AMBIENTE', 'Analgesico y antipiretico de uso comun',
 TRUE, 20, (CURRENT_DATE + INTERVAL '360 day')::date),
-- Bajo stock (stock < minimo)
('Ibuprofen 400mg', 'Ibuprofeno', '400 mg', 'TABLETA', 'Blister x 10 tabletas',
 'IBU-001', 5, 3200.00, 120, 1700.00, 3200.00, FALSE,
 'MK', 'RS-IBU-002', 'ORAL', 'BLISTER',
 'Bodega A2', 'AMBIENTE', 'AINE para dolor e inflamacion',
 TRUE, 15, (CURRENT_DATE + INTERVAL '220 day')::date),
-- Agotado (stock = 0)
('Loratadina 10 mg', 'Loratadina', '10 mg', 'TABLETA', 'Caja x 10 tabletas',
 'LOT-001', 0, 6400.00, 100, 3200.00, 6400.00, FALSE,
 'La Sante', 'RS-LOR-003', 'ORAL', 'CAJA',
 'Bodega A3', 'AMBIENTE', 'Antihistaminico no sedante',
 TRUE, 10, (CURRENT_DATE + INTERVAL '180 day')::date),
-- Proximo a vencer (<= 15 dias)
('Amoxicillin 500mg', 'Amoxicilina', '500 mg', 'CAPSULA', 'Caja x 12 capsulas',
 'AMX-001', 30, 7800.00, 140, 4100.00, 7800.00, TRUE,
 'Siegfried', 'RS-AMX-004', 'ORAL', 'CAJA',
 'Bodega B1', 'AMBIENTE', 'Antibiotico betalactamico',
 TRUE, 10, (CURRENT_DATE + INTERVAL '8 day')::date),
-- Vencido
('Omeprazol 20 mg', 'Omeprazol', '20 mg', 'CAPSULA', 'Caja x 14 capsulas',
 'OMP-001', 25, 9800.00, 160, 5100.00, 9800.00, FALSE,
 'Procaps', 'RS-OMP-005', 'ORAL', 'CAJA',
 'Bodega B2', 'AMBIENTE', 'Protector gastrico',
 TRUE, 12, (CURRENT_DATE - INTERVAL '7 day')::date),
-- Vencido y agotado
('Diclofenaco 50 mg', 'Diclofenaco', '50 mg', 'TABLETA', 'Caja x 20 tabletas',
 'DCF-001', 0, 7600.00, 120, 3900.00, 7600.00, FALSE,
 'Tecnoquimicas', 'RS-DCF-006', 'ORAL', 'CAJA',
 'Bodega B3', 'AMBIENTE', 'AINE con control de rotacion por vencimiento',
 TRUE, 10, (CURRENT_DATE - INTERVAL '35 day')::date),
-- Proximo a vencer (16-30 dias)
('Vitamina C 1 g', 'Acido Ascorbico', '1 g', 'TABLETA', 'Tubo x 10 tabletas',
 'VIC-001', 40, 6900.00, 180, 3500.00, 6900.00, FALSE,
 'Bayer', 'RS-VIC-007', 'ORAL', 'TUBO',
 'Bodega C1', 'AMBIENTE', 'Suplemento vitaminico',
 TRUE, 18, (CURRENT_DATE + INTERVAL '25 day')::date),
-- Bajo stock adicional
('Salbutamol Inhalador', 'Salbutamol', '100 mcg/dosis', 'OTRO', 'Inhalador 200 dosis',
 'SAT-001', 9, 28900.00, 60, 15000.00, 28900.00, TRUE,
 'GSK', 'RS-SAL-008', 'INHALATORIA', 'FRASCO',
 'Bodega C2', 'AMBIENTE', 'Broncodilatador de rescate',
 TRUE, 10, (CURRENT_DATE + INTERVAL '120 day')::date),
-- Activo sin alertas
('Metformina 850 mg', 'Metformina', '850 mg', 'TABLETA', 'Caja x 30 tabletas',
 'MET-001', 90, 4500.00, 220, 2300.00, 4500.00, TRUE,
 'Lafrancol', 'RS-MET-009', 'ORAL', 'CAJA',
 'Bodega C3', 'AMBIENTE', 'Antidiabetico oral',
 TRUE, 15, (CURRENT_DATE + INTERVAL '300 day')::date),
-- Proximo a vencer (31-60 dias)
('Simvastatina 20 mg', 'Simvastatina', '20 mg', 'TABLETA', 'Caja x 30 tabletas',
 'SIM-001', 75, 5200.00, 200, 2600.00, 5200.00, TRUE,
 'Pfizer', 'RS-SIM-010', 'ORAL', 'CAJA',
 'Bodega D1', 'AMBIENTE', 'Hipolipemiante',
 TRUE, 10, (CURRENT_DATE + INTERVAL '45 day')::date),
-- Activo sin alertas
('Losartan 50 mg', 'Losartan', '50 mg', 'TABLETA', 'Caja x 30 tabletas',
 'LOS-001', 75, 14100.00, 210, 7100.00, 14100.00, TRUE,
 'Sandoz', 'RS-LOS-011', 'ORAL', 'CAJA',
 'Bodega D2', 'AMBIENTE', 'Antihipertensivo',
 TRUE, 12, (CURRENT_DATE + INTERVAL '280 day')::date);

-- =========================================
-- SAMPLE DATA: batch (one initial batch per product)
-- =========================================
INSERT INTO batch (product_id, batch_code, expiration_date, initial_stock, available_stock, status)
SELECT
    id,
    CONCAT('INIT-', code),
    expirationdate,
    stock,
    stock,
    CASE
        WHEN expirationdate < CURRENT_DATE THEN 'EXPIRED'
        WHEN stock = 0 THEN 'OUT_OF_STOCK'
        ELSE 'ACTIVE'
    END
FROM product;

-- =========================================
-- SAMPLE DATA: motion
-- Initial records linked to products
-- =========================================
INSERT INTO motion (type, amount, reason, user_name, user_email, user_role, status, produc_id, batch_id)
SELECT
    'Entrance',
    p.stock,
    'Carga inicial de catálogo',
    'SYSTEM_INIT',
    'system@farmaexpres.local',
    'SYSTEM',
    'NORMAL',
    p.id,
    b.id
FROM product p
JOIN batch b
  ON b.product_id = p.id
 AND b.batch_code = CONCAT('INIT-', p.code);
