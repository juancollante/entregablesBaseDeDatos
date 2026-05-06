-- Plantillas base (MVP)

INSERT INTO plantilla_notificacion (id, codigo, asunto, cuerpo, activo, updated_at)
VALUES
 ('30000000-0000-0000-0000-000000000001','SHIPMENT_CREATED','Envío creado','Se registró un envío en el sistema.',true,now()),
 ('30000000-0000-0000-0000-000000000002','TRACKING_EVENT_REGISTERED','Actualización de seguimiento','Se registró un evento de seguimiento.',true,now()),
 ('30000000-0000-0000-0000-000000000003','GENERICA','Notificación','Se recibió un evento del sistema.',true,now())
ON CONFLICT (codigo) DO NOTHING;

