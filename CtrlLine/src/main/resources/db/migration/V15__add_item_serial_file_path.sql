ALTER TABLE item_serial
    ADD COLUMN IF NOT EXISTS serial_file_path VARCHAR(512) NULL;
