-- 为论坛搜索功能添加全文索引
-- 执行此脚本来优化搜索性能

USE vcampus;

-- 为 forum_threads 表的 title 和 content 字段添加全文索引
ALTER TABLE forum_threads ADD FULLTEXT INDEX ft_title_content (title, content);

-- 为 forum_posts 表的 content 字段添加全文索引  
ALTER TABLE forum_posts ADD FULLTEXT INDEX ft_content (content);

-- 显示已创建的索引
SHOW INDEX FROM forum_threads WHERE Key_name LIKE 'ft_%';
SHOW INDEX FROM forum_posts WHERE Key_name LIKE 'ft_%';

-- 测试全文搜索查询（可选）
-- SELECT * FROM forum_threads WHERE MATCH(title, content) AGAINST('测试关键词' IN NATURAL LANGUAGE MODE);
-- SELECT * FROM forum_posts WHERE MATCH(content) AGAINST('测试关键词' IN NATURAL LANGUAGE MODE);
