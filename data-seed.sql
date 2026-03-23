-- Semillas de datos para pruebas Zentinel
-- Crear Áreas
INSERT INTO areas (nombre, descripcion) VALUES ('PRODUCCIÓN', 'Área de manufactura central');
INSERT INTO areas (nombre, descripcion) VALUES ('MANTENIMIENTO', 'Departamento técnico');
INSERT INTO areas (nombre, descripcion) VALUES ('SISTEMAS', 'Oficina de TI');

-- Crear Tipos de Producto
INSERT INTO tipo_producto (nombre, descripcion) VALUES ('HERRAMIENTA', 'Herramientas manuales y eléctricas');
INSERT INTO tipo_producto (nombre, descripcion) VALUES ('CONSUMIBLE', 'Material de oficina y limpieza');
INSERT INTO tipo_producto (nombre, descripcion) VALUES ('REFACCIÓN', 'Piezas de repuesto para maquinaria');

-- Crear Empresas
INSERT INTO empresa (id, codigo, nombre, nombre_completo, rfc) VALUES (1, 'ZEN01', 'Zentinel Corp', 'Zentinel S.A. de C.V.', 'ZEN123456789');

-- Crear Almacenes
INSERT INTO almacenes (id, nombre, ubicacion, empresa_id, activo) VALUES (1, 'Almacén Central', 'Planta Baja', 1, TRUE);
INSERT INTO almacenes (id, nombre, ubicacion, empresa_id, activo) VALUES (2, 'Almacén Secundario', 'Patio Norte', 1, TRUE);

-- Crear Tipos de Cliente (Opcional para pruebas)
INSERT INTO tipos_cliente (nombre, descripcion, margen_utilidad) VALUES ('GENERAL', 'Cliente estándar', 15.00);

-- Crear Usuarios (Contraseña: password123)
-- Admin
INSERT INTO usuarios (usuario, nombre, apellido_paterno, correo, rol, password, activo) 
VALUES ('admin', 'Admin', 'Zentinel', 'admin@zentinel.com', 'ADMIN', '$2a$10$8.UnVuG9HHgffUDAlk8ufOnl566clas9962eH.Q9N/S4Y3C5qR4b6', TRUE);

-- Mostrador
INSERT INTO usuarios (usuario, nombre, apellido_paterno, correo, rol, password, activo) 
VALUES ('mostrador', 'Eliel', 'Mauro', 'mostrador@zentinel.com', 'MOSTRADOR', '$2a$10$8.UnVuG9HHgffUDAlk8ufOnl566clas9962eH.Q9N/S4Y3C5qR4b6', TRUE);

-- Asignar Almacenes a Usuarios
INSERT INTO usuario_almacen (usuario, almacen_id) VALUES ('admin', 1);
INSERT INTO usuario_almacen (usuario, almacen_id) VALUES ('admin', 2);
INSERT INTO usuario_almacen (usuario, almacen_id) VALUES ('mostrador', 1);

-- Crear Productos
INSERT INTO productos (sku, nombre, unidad_medida, tipo_producto_id, precio_unitario, activo) 
VALUES ('TOOL-001', 'Martillo Industrial', 'PZA', 1, 250.00, TRUE);
INSERT INTO productos (sku, nombre, unidad_medida, tipo_producto_id, precio_unitario, activo) 
VALUES ('CONS-001', 'Papel Bond A4', 'PAQUETE', 2, 85.00, TRUE);
INSERT INTO productos (sku, nombre, unidad_medida, tipo_producto_id, precio_unitario, activo) 
VALUES ('REF-001', 'Rodamiento 6204', 'PZA', 3, 120.00, TRUE);

-- Inventario Inicial
INSERT INTO inventario (producto_sku, almacen_id, cantidad, ubicacion_fisica) VALUES ('TOOL-001', 1, 10, 'A-1');
INSERT INTO inventario (producto_sku, almacen_id, cantidad, ubicacion_fisica) VALUES ('CONS-001', 1, 50, 'B-2');
INSERT INTO inventario (producto_sku, almacen_id, cantidad, ubicacion_fisica) VALUES ('REF-001', 1, 4, 'C-3'); -- Producto con menos de 5 para prueba
