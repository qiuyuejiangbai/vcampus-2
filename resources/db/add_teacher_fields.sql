-- 为teachers表添加title和office字段
-- 这个脚本用于修复教师信息更新失败的问题

USE vcampus;

-- 添加title字段（职称）
ALTER TABLE teachers ADD COLUMN title VARCHAR(100) COMMENT '职称' AFTER department;

-- 添加office字段（办公室）
ALTER TABLE teachers ADD COLUMN office VARCHAR(200) COMMENT '办公室' AFTER title;

-- 为新增字段添加索引
CREATE INDEX idx_teachers_title ON teachers (title);
CREATE INDEX idx_teachers_office ON teachers (office);

-- 更新现有数据，为title和office字段设置默认值（可选）
-- UPDATE teachers SET title = '讲师' WHERE title IS NULL;
-- UPDATE teachers SET office = '待定' WHERE office IS NULL;

-- 显示表结构确认修改
DESCRIBE teachers;
