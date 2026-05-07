

INSERT INTO municipios (id, codigo_dane, nombre, activo)
VALUES
 ('20000000-0000-0000-0000-000000000010', '05266', 'Envigado', true),
 ('20000000-0000-0000-0000-000000000011', '05615', 'Rionegro', true),
 ('20000000-0000-0000-0000-000000000012', '05376', 'La Ceja', true),
 ('20000000-0000-0000-0000-000000000013', '05308', 'Girardota', true),
 ('20000000-0000-0000-0000-000000000014', '05212', 'Copacabana', true),
 ('20000000-0000-0000-0000-000000000015', '05440', 'Marinilla', true),
 ('20000000-0000-0000-0000-000000000016', '05467', 'El Retiro', true),
 ('20000000-0000-0000-0000-000000000017', '05380', 'Sabaneta', true),
 ('20000000-0000-0000-0000-000000000018', '05059', 'Armenia', true),
 ('20000000-0000-0000-0000-000000000019', '05154', 'Caucasia', true)
ON CONFLICT (codigo_dane) DO NOTHING;

-- Sedes enlazadas por codigo_dane del municipio (incluye Bello e Itagüí ya sembrados en V2).
INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000011', 'SEDE-BEL-01', 'Sede Bello Norte', m.id, 'Zona norte', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05088'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000012', 'SEDE-ITG-01', 'Sede Itagüí', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05360'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000013', 'SEDE-ENV-01', 'Sede Envigado', m.id, 'Calle principal', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05266'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000014', 'SEDE-RNG-01', 'Sede Rionegro', m.id, 'Zona industrial', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05615'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000015', 'SEDE-LCE-01', 'Sede La Ceja', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05376'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000016', 'SEDE-GIR-01', 'Sede Girardota', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05308'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000017', 'SEDE-COP-01', 'Sede Copacabana', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05212'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000018', 'SEDE-MAR-01', 'Sede Marinilla', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05440'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-000000000019', 'SEDE-ELR-01', 'Sede El Retiro', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05467'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-00000000001a', 'SEDE-SAB-01', 'Sede Sabaneta', m.id, 'Parque', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05380'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-00000000001b', 'SEDE-ARM-01', 'Sede Armenia (Ant.)', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05059'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-00000000001c', 'SEDE-CAU-01', 'Sede Caucasia', m.id, 'Centro', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05154'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO sedes (id, codigo, nombre, municipio_id, direccion, telefono, tipo, activo, created_at)
SELECT '21000000-0000-0000-0000-00000000001d', 'SEDE-MDE-02', 'Sede Medellín Laureles', m.id, 'Laureles', '', 'OFICINA', true, now()
FROM municipios m WHERE m.codigo_dane = '05001'
ON CONFLICT (codigo) DO NOTHING;
