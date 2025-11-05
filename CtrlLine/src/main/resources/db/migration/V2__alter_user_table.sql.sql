ALTER TABLE `user`
    ADD COLUMN `user_extension` VARCHAR(32) NOT NULL COMMENT '내선번호';

ALTER TABLE `user`
    ADD UNIQUE INDEX `UK_USER_EMAIL` (`user_email`);