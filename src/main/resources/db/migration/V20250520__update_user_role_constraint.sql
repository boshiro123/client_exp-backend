-- Удаляем существующее ограничение
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Добавляем новое ограничение с поддержкой роли PENDING
ALTER TABLE users ADD CONSTRAINT users_role_check 
CHECK (role IN ('ADMIN', 'MANAGER', 'PENDING')); 