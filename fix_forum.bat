@echo off
echo 正在修复论坛数据...

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_sections (name, description, sort_order, status) VALUES ('学术交流', '课程/学术问题讨论', 1, 1), ('校园生活', '生活/活动/社团', 2, 1), ('二手交易', '闲置物品交易', 3, 1), ('失物招领', '失物与招领信息', 4, 1), ('求助咨询', '问题求助与咨询', 5, 1);"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_threads (title, content, author_id, section_id, category, is_essence, reply_count, view_count, like_count, favorite_count, is_pinned, is_locked, status, created_time, last_post_time) VALUES ('欢迎来到vCampus虚拟校园！', '大家好，欢迎使用vCampus虚拟校园系统！这里是我们的交流平台，可以分享学习心得、讨论课程内容、交流生活感悟。让我们一起营造一个积极向上的学习氛围！', 1, 1, '公告', TRUE, 2, 150, 2, 5, TRUE, FALSE, 1, '2024-03-05 09:00:00', '2024-03-07 14:20:00'), ('Java程序设计课程讨论', '有同学在学习Java面向对象编程时遇到困难吗？欢迎在这里讨论交流，互相帮助！', 2, 1, '学习交流', FALSE, 3, 89, 1, 2, FALSE, FALSE, 1, '2024-03-08 10:15:00', '2024-03-10 09:15:00'), ('图书馆新书推荐', '最近图书馆新进了一批计算机类书籍，推荐大家借阅学习。特别推荐《算法导论》和《深入理解计算机系统》。', 3, 2, '图书推荐', FALSE, 1, 67, 0, 1, FALSE, FALSE, 1, '2024-03-09 15:30:00', '2024-03-10 11:30:00'), ('校园商店优惠活动', '本月校园商店文具用品八折优惠，需要购买学习用品的同学不要错过哦！', 1, 2, '活动通知', FALSE, 0, 45, 0, 0, FALSE, FALSE, 1, '2024-03-12 08:45:00', NULL);"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_posts (thread_id, content, author_id, parent_post_id, reply_level, reply_path, like_count, created_time) VALUES (1, '系统界面很友好，功能很全面，点赞！', 5, NULL, 0, '1', 1, '2024-03-06 10:30:00'), (1, '期待更多功能的上线，加油！', 6, NULL, 0, '2', 0, '2024-03-07 14:20:00'), (2, '我在学习继承和多态时有些困惑，有同学能帮忙解答一下吗？', 7, NULL, 0, '3', 0, '2024-03-09 16:45:00'), (2, '建议多做练习题，理论结合实践才能更好理解。', 8, 3, 1, '3/4', 0, '2024-03-09 17:10:00'), (2, '可以参考一下《Java核心技术》这本书，讲得很详细。', 5, NULL, 0, '5', 1, '2024-03-10 09:15:00'), (3, '《算法导论》确实是经典教材，值得深入学习！', 9, NULL, 0, '6', 0, '2024-03-10 11:30:00');"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_tags (tag_name) VALUES ('Java'), ('学习'), ('公告'), ('活动'), ('图书');"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_thread_tags (thread_id, tag_id) VALUES (1, 3), (1, 2), (2, 1), (2, 2), (3, 5), (4, 4);"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_likes (entity_type, entity_id, user_id, created_time) VALUES ('thread', 1, 5, '2024-03-06 10:35:00'), ('thread', 1, 6, '2024-03-07 14:25:00'), ('thread', 2, 7, '2024-03-09 16:50:00'), ('post', 1, 6, '2024-03-07 14:25:00'), ('post', 3, 8, '2024-03-09 17:15:00');"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_favorites (thread_id, user_id, created_time) VALUES (1, 5, '2024-03-06 10:40:00'), (2, 6, '2024-03-08 10:20:00');"

mysql -u root -p vcampus -e "INSERT IGNORE INTO forum_announcements (title, content, is_pinned, status, created_time) VALUES ('欢迎使用校园论坛系统！', '请遵守论坛规则，文明发言。', TRUE, 1, '2024-03-05 08:30:00'), ('期末考试安排已发布', '请进入教务系统查看详细安排。', TRUE, 1, '2024-03-08 09:00:00');"

echo 论坛数据修复完成！
echo 检查数据状态：
mysql -u root -p vcampus -e "SELECT 'forum_threads' as table_name, COUNT(*) as count FROM forum_threads UNION ALL SELECT 'forum_posts', COUNT(*) FROM forum_posts UNION ALL SELECT 'forum_sections', COUNT(*) FROM forum_sections UNION ALL SELECT 'forum_likes', COUNT(*) FROM forum_likes UNION ALL SELECT 'forum_favorites', COUNT(*) FROM forum_favorites;"

pause
