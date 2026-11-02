-- Script de migración: asignar comunidades autónomas a universidades existentes
-- Ejecutar una sola vez en MariaDB
-- Uso: docker exec -i openscope-mariadb-1 mariadb -u oamonitor -poamonitor oamonitor < migration_autonomous_communities.sql

-- Paso 1: Añadir la columna (si Hibernate no la ha creado automáticamente)
ALTER TABLE institution ADD COLUMN IF NOT EXISTS autonomous_community VARCHAR(255);

-- Paso 2: Asignar comunidades autónomas
UPDATE institution SET autonomous_community = 'Madrid' WHERE rebiun_code IN ('UCM', 'UAM', 'UPM', 'UC3M', 'URJC', 'UAH', 'UNED', 'CEU', 'UFV', 'UCJC', 'UEM', 'NEBRIJA', 'UAX', 'IE', 'UDIMA');

UPDATE institution SET autonomous_community = 'Cataluña' WHERE rebiun_code IN ('UB', 'UAB', 'UPC', 'UPF', 'UDG', 'UDL', 'URV', 'UOC', 'UVIC-UCC', 'URL', 'UIC', 'UAO-CEU');

UPDATE institution SET autonomous_community = 'Andalucía' WHERE rebiun_code IN ('UGR', 'US', 'UMA', 'UJAEN', 'UCA', 'UCO', 'UAL', 'UHU', 'UPO', 'LOYOLA', 'UNIA');

UPDATE institution SET autonomous_community = 'C. Valenciana' WHERE rebiun_code IN ('UV', 'UJI', 'UA', 'UMH', 'UPV', 'UCV', 'UCH-CEU');

UPDATE institution SET autonomous_community = 'País Vasco' WHERE rebiun_code IN ('UPV-EHU', 'DEUSTO', 'MONDRAGON');

UPDATE institution SET autonomous_community = 'Castilla y León' WHERE rebiun_code IN ('USAL', 'UBU', 'UNILEON', 'UVA', 'UI1', 'UCAV', 'UEMC');

UPDATE institution SET autonomous_community = 'Galicia' WHERE rebiun_code IN ('USC', 'UDC', 'UVIGO');

UPDATE institution SET autonomous_community = 'Canarias' WHERE rebiun_code IN ('ULL', 'ULPGC');

UPDATE institution SET autonomous_community = 'Aragón' WHERE rebiun_code = 'UNIZAR';
UPDATE institution SET autonomous_community = 'Asturias' WHERE rebiun_code = 'UNIOVI';
UPDATE institution SET autonomous_community = 'Baleares' WHERE rebiun_code = 'UIB';
UPDATE institution SET autonomous_community = 'Cantabria' WHERE rebiun_code IN ('UNICAN', 'COMILLAS', 'UIMP');
UPDATE institution SET autonomous_community = 'Castilla-La Mancha' WHERE rebiun_code = 'UCLM';
UPDATE institution SET autonomous_community = 'Extremadura' WHERE rebiun_code = 'UNEX';
UPDATE institution SET autonomous_community = 'La Rioja' WHERE rebiun_code IN ('UNIRIOJA', 'UNIR');
UPDATE institution SET autonomous_community = 'Murcia' WHERE rebiun_code IN ('UM', 'UPCT');
UPDATE institution SET autonomous_community = 'Navarra' WHERE rebiun_code IN ('UNAV', 'UPNA');

-- Verificación
SELECT rebiun_code, name, autonomous_community FROM institution ORDER BY autonomous_community, name;

-- Resumen por comunidad
SELECT autonomous_community, COUNT(*) as num_universidades FROM institution GROUP BY autonomous_community ORDER BY num_universidades DESC;
