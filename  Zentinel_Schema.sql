-- Zentinel - Database Schema provided by User
-- ==========================================
-- 1. TABLAS DE CATÁLOGOS Y MAESTRAS (Nivel 1)
-- ==========================================

CREATE TABLE IF NOT EXISTS areas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS tipo_producto (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS tipos_cliente (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    margen_utilidad NUMERIC(5,2)
);

CREATE TABLE IF NOT EXISTS proveedores (
    id SERIAL PRIMARY KEY,
    nombre_corto VARCHAR(50) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    rfc VARCHAR(13),
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    contacto VARCHAR(100),
    correo VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE
);

-- Note: The existing usuarios table might need adjustment to match exactly
-- But we already have a 'usuarios' table with 'password' from Spring Security setup.

-- ==========================================
-- 2. ESTRUCTURA ORGANIZACIONAL (Nivel 2)
-- ==========================================

CREATE TABLE IF NOT EXISTS empresa (
    id INT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(200),
    rfc VARCHAR(13) NOT NULL
);

CREATE TABLE IF NOT EXISTS organizacion (
    id INT PRIMARY KEY,
    nombre_corto VARCHAR(50) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    departamento VARCHAR(50),
    empresa_id INTEGER REFERENCES empresa(id)
);

CREATE TABLE IF NOT EXISTS almacenes (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(200),
    empresa_id INTEGER REFERENCES empresa(id),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS clientes (
    id INT PRIMARY KEY,
    nombre_corto VARCHAR(50) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    rfc VARCHAR(13),
    curp VARCHAR(18),
    telefono VARCHAR(20),
    contacto_adicional VARCHAR(100),
    correo VARCHAR(100),
    tipo_cliente_id INTEGER REFERENCES tipos_cliente(id),
    empresa_id INTEGER REFERENCES empresa(id),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. PRODUCTOS E INVENTARIO (Nivel 3)
-- ==========================================

CREATE TABLE IF NOT EXISTS productos (
    sku VARCHAR(50) PRIMARY KEY,
    codigo_interno VARCHAR(50),
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    marca VARCHAR(50),
    modelo VARCHAR(50),
    unidad_medida VARCHAR(20) NOT NULL,
    tipo_producto_id INTEGER REFERENCES tipo_producto(id),
    tiempo_entrega_dias INTEGER,
    precio_unitario NUMERIC(12,2) NOT NULL,
    iva_porcentaje NUMERIC(4,2),
    imagen_url VARCHAR(255),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS inventario (
    id SERIAL PRIMARY KEY,
    producto_sku VARCHAR(50) REFERENCES productos(sku),
    almacen_id INTEGER REFERENCES almacenes(id),
    cantidad NUMERIC(12,4) NOT NULL,
    ubicacion_fisica VARCHAR(100),
    punto_reorden NUMERIC(12,4),
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_actualizacion VARCHAR(50) REFERENCES usuarios(usuario)
);

-- ==========================================
-- 4. MOVIMIENTOS: ENTRADAS Y SALIDAS (Nivel 4)
-- ==========================================

CREATE TABLE IF NOT EXISTS entradas (
    folio_factura VARCHAR(50) PRIMARY KEY,
    id SERIAL UNIQUE,
    proveedor_id INTEGER REFERENCES proveedores(id),
    fecha_factura TIMESTAMP NOT NULL,
    salida_relacionada_id INTEGER,
    fecha_recepcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    num_pp VARCHAR(50),
    comentarios VARCHAR(255),
    almacen_destino_id INTEGER REFERENCES almacenes(id),
    estatus VARCHAR(20),
    iva NUMERIC(14,2),
    total NUMERIC(14,2),
    tipo_cambio NUMERIC(8,4),
    cancelado BOOLEAN DEFAULT FALSE,
    fecha_cancelacion TIMESTAMP,
    usuario_cancelacion VARCHAR(50) REFERENCES usuarios(usuario),
    motivo_cancelacion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS entradas_detalle (
    id SERIAL PRIMARY KEY,
    entrada_id INTEGER REFERENCES entradas(id),
    producto_sku VARCHAR(50) REFERENCES productos(sku),
    cantidad NUMERIC(12,4) NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL,
    subtotal_linea NUMERIC(14,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS salidas (
    folio SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_solicitante VARCHAR(50) REFERENCES usuarios(usuario),
    usuario_atendio VARCHAR(50) REFERENCES usuarios(usuario),
    departamento VARCHAR(100),
    n_reporte VARCHAR(50),
    num_presupuesto VARCHAR(50),
    descripcion VARCHAR(500),
    estatus VARCHAR(20),
    almacen_origen_id INTEGER REFERENCES almacenes(id),
    subtotal NUMERIC(14,2),
    iva NUMERIC(14,2),
    total NUMERIC(14,2),
    cancelado BOOLEAN DEFAULT FALSE,
    fecha_cancelacion TIMESTAMP,
    usuario_cancelacion VARCHAR(50) REFERENCES usuarios(usuario),
    motivo_cancelacion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS salidas_detalle (
    id SERIAL PRIMARY KEY,
    salida_folio INTEGER REFERENCES salidas(folio),
    producto_sku VARCHAR(50) REFERENCES productos(sku),
    cantidad_solicitada NUMERIC(12,4) NOT NULL,
    cantidad_entregada NUMERIC(12,4) NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL,
    subtotal_linea NUMERIC(14,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS cancelaciones (
    id SERIAL PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL,
    documento_id INTEGER NOT NULL,
    fecha_cancelacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_cancelacion VARCHAR(50) REFERENCES usuarios(usuario),
    motivo VARCHAR(255) NOT NULL
);
