ALTER TABLE `factory`
ADD CONSTRAINT fk_factory_user_id
FOREIGN KEY (`user_id`)
REFERENCES `user`(`user_id`)
ON UPDATE CASCADE;

ALTER TABLE `factory`
ADD CONSTRAINT uq_factory_code UNIQUE (`factory_code`);
