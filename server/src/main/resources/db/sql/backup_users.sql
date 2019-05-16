-- 1. 将用户信息复制到临时表
SELECT * INTO temp_user_info FROM user_info;
SELECT * INTO temp_user_bind FROM user_bind;
SELECT * INTO temp_user_avatar FROM user_avatar;
SELECT * INTO temp_persistent_logins FROM persistent_logins;

-- 2. 删除表
-- 3. 部署完成后，恢复用户信息
INSERT INTO user_info SELECT * from temp_user_info;
INSERT INTO user_bind SELECT * from temp_user_bind;
INSERT INTO user_avatar SELECT * from temp_user_avatar;
INSERT INTO persistent_logins SELECT * from temp_persistent_logins;

-- 4. 删除临时表
DROP TABLE temp_user_info;
DROP TABLE temp_user_bind;
DROP TABLE temp_user_avatar;
DROP TABLE temp_persistent_logins;
