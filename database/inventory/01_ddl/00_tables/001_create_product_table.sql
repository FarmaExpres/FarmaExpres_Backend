CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    nombre_generico VARCHAR(200),
    concentracion VARCHAR(100),
    forma_farmaceutica VARCHAR(100),
    presentacion VARCHAR(150),
    code VARCHAR(50) NOT NULL UNIQUE,
    stock INTEGER NOT NULL,
    unitprice NUMERIC(12,2) NOT NULL,
    stock_maximo INTEGER,
    precio_compra NUMERIC(12,2),
    precio_venta NUMERIC(12,2),
    requiere_receta BOOLEAN,
    laboratorio VARCHAR(150),
    registro_sanitario VARCHAR(150),
    via_administracion VARCHAR(100),
    unidad_medida VARCHAR(60),
    ubicacion_almacen VARCHAR(150),
    temperatura_conservacion VARCHAR(100),
    observaciones TEXT,
    asset BOOLEAN NOT NULL DEFAULT TRUE,
    minimumstock INTEGER NOT NULL,
    expirationdate DATE NOT NULL,
    CONSTRAINT chk_product_stock_non_negative
        CHECK (stock >= 0),
    CONSTRAINT chk_product_minimum_stock_non_negative
        CHECK (minimumstock >= 0),
    CONSTRAINT chk_product_unitprice_non_negative
        CHECK (unitprice >= 0),
    CONSTRAINT chk_product_purchase_price_non_negative
        CHECK (precio_compra IS NULL OR precio_compra >= 0),
    CONSTRAINT chk_product_sale_price_non_negative
        CHECK (precio_venta IS NULL OR precio_venta >= 0),
    CONSTRAINT chk_product_stock_max_non_negative
        CHECK (stock_maximo IS NULL OR stock_maximo >= 0),
    CONSTRAINT chk_product_forma_farmaceutica
        CHECK (
            forma_farmaceutica IS NULL OR
            forma_farmaceutica IN ('TABLETA', 'CAPSULA', 'JARABE', 'SUSPENSION', 'INYECTABLE', 'CREMA', 'GOTAS', 'AMPOLLA', 'SUPOSITORIO', 'OTRO')
        ),
    CONSTRAINT chk_product_via_administracion
        CHECK (
            via_administracion IS NULL OR
            via_administracion IN ('ORAL', 'INTRAVENOSA', 'INTRAMUSCULAR', 'SUBCUTANEA', 'TOPICA', 'INHALATORIA', 'OFTALMICA', 'OTICA', 'NASAL', 'RECTAL', 'VAGINAL', 'OTRA')
        ),
    CONSTRAINT chk_product_unidad_medida
        CHECK (
            unidad_medida IS NULL OR
            unidad_medida IN ('UNIDAD', 'BLISTER', 'CAJA', 'FRASCO', 'VIAL', 'AMPOLLA', 'TUBO', 'SOBRE', 'JERINGA', 'UI', 'MCG', 'ML', 'MG', 'G')
        ),
    CONSTRAINT chk_product_temperatura_conservacion
        CHECK (
            temperatura_conservacion IS NULL OR
            temperatura_conservacion IN ('AMBIENTE', 'REFRIGERADO', 'CONGELADO', 'CONTROLADA', 'NO_APLICA')
        )
);
