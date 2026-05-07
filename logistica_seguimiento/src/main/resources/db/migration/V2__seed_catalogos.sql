-- Seeds mínimos para demo/MVP

-- Municipios (Antioquia) - subset
INSERT INTO municipios (id, codigo_dane, nombre, activo)
VALUES
 ('20000000-0000-0000-0000-000000000001','05001','Medellín',true),
 ('20000000-0000-0000-0000-000000000002','05088','Bello',true),
 ('20000000-0000-0000-0000-000000000003','05360','Itagüí',true)
ON CONFLICT (codigo_dane) DO NOTHING;

-- Sedes
INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
VALUES
 ('21000000-0000-0000-0000-000000000001','SEDE-MDE-01','Sede Medellín Centro','20000000-0000-0000-0000-000000000001','Centro','', 'OFICINA', true, now())
ON CONFLICT (codigo) DO NOTHING;

-- Tipos de evento
INSERT INTO tipos_evento_seguimiento (id, codigo, nombre, descripcion, orden_visual, activo)
VALUES
 ('22000000-0000-0000-0000-000000000001','RECIBIDO_SEDE','Recibido en sede','Ingreso a sede',1,true),
 ('22000000-0000-0000-0000-000000000002','EN_TRANSITO','En tránsito','En tránsito hacia destino',2,true),
 ('22000000-0000-0000-0000-000000000003','ENTREGADO','Entregado','Entregado al destinatario',3,true)
ON CONFLICT (codigo) DO NOTHING;

