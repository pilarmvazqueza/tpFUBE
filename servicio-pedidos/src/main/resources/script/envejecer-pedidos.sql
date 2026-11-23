UPDATE pedidos
SET fecha_creacion = now() - INTERVAL '2 days',
    fecha_actualizacion = now() - INTERVAL '2 days'
WHERE estado = 'CREADO';

/*
Para ejecutar este script, se debe poner en la terminal desde la raiz del proyecto:
Get-Content -Raw servicio-pedidos/src/main/resources/script/envejecer-pedidos.sql | docker exec -i postgres-tp psql -U postgres -d pedidos_db
*/