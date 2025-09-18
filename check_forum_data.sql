-- 检查论坛数据
USE vcampus;

-- 检查论坛主题数量
SELECT 'forum_threads' as table_name, COUNT(*) as count FROM forum_threads WHERE status = 1;

-- 检查论坛回复数量
SELECT 'forum_posts' as table_name, COUNT(*) as count FROM forum_posts WHERE status = 1;

-- 检查特定主题的回复
SELECT thread_id, COUNT(*) as reply_count 
FROM forum_posts 
WHERE status = 1 
GROUP BY thread_id 
ORDER BY thread_id;

-- 检查主题1的回复详情
SELECT post_id, thread_id, content, author_id, created_time 
FROM forum_posts 
WHERE thread_id = 1 AND status = 1 
ORDER BY created_time;
