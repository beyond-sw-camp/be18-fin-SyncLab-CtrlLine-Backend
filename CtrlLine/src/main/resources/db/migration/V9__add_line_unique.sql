ALTER TABLE `line`
DROP INDEX IF EXISTS uq_line_code;

ALTER TABLE `line`
    ADD CONSTRAINT uq_line_code UNIQUE (`line_code`);
