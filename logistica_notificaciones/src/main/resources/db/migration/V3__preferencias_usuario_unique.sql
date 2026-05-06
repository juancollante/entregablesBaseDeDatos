-- Preferencias por usuario externo (tablas_bases_de_datos.txt: usuario_id_externo o email)

CREATE UNIQUE INDEX IF NOT EXISTS uq_pref_usuario_evento_canal
    ON preferencias_notificaciones (usuario_id_externo, codigo_evento, canal)
    WHERE usuario_id_externo IS NOT NULL;
