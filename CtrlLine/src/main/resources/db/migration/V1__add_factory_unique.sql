ALTER TABLE `factory`
DROP INDEX IF EXISTS uq_factory_code;

ALTER TABLE `factory`
ADD CONSTRAINT uq_factory_code UNIQUE (`factory_code`);
