CREATE DATABASE tracking_logistico;

USE tracking_logistico;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE envios (
    id_envio INT AUTO_INCREMENT PRIMARY KEY,
    codigo_seguimiento VARCHAR(50) NOT NULL UNIQUE,

    nombre_remitente VARCHAR(100) NOT NULL,
    telefono_remitente VARCHAR(20) NOT NULL,
    ciudad_origen VARCHAR(100) NOT NULL,
    direccion_origen VARCHAR(150) NOT NULL,

    nombre_destinatario VARCHAR(100) NOT NULL,
    telefono_destinatario VARCHAR(20) NOT NULL,
    ciudad_destino VARCHAR(100) NOT NULL,
    direccion_destino VARCHAR(150) NOT NULL,

    tipo_paquete VARCHAR(50) NOT NULL,
    peso DECIMAL(10,2) NOT NULL,

    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_estimada_entrega DATE NOT NULL,

    estado_actual VARCHAR(50) DEFAULT 'Registrado'
);

CREATE TABLE consultas_tracking (
    id_consulta INT AUTO_INCREMENT PRIMARY KEY,
    codigo_seguimiento VARCHAR(50) NOT NULL,
    fecha_consulta DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO usuarios (username, password_hash, nombre_completo)
VALUES ('admin', '123456', 'Administrador');

SELECT * FROM usuarios;

INSERT INTO envios (
    codigo_seguimiento,
    nombre_remitente,
    telefono_remitente,
    ciudad_origen,
    direccion_origen,
    nombre_destinatario,
    telefono_destinatario,
    ciudad_destino,
    direccion_destino,
    tipo_paquete,
    peso,
    fecha_estimada_entrega
) VALUES (
    'ABC123456',
    'Juan Perez',
    '3001234567',
    'Medellin',
    'Calle 10 #20-30',
    'Maria Lopez',
    '3109876543',
    'Bogota',
    'Carrera 50 #40-20',
    'Caja',
    2.5,
    '2026-04-10'
);

SELECT codigo_seguimiento, estado_actual, fecha_registro
FROM envios
WHERE codigo_seguimiento = 'ABC123456';
