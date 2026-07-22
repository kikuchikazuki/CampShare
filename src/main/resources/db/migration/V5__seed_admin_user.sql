INSERT INTO users (display_name, email, password_hash, role_id)
SELECT 'CampShare 管理者', 'admin@campshare.local',
       '$2a$10$5Euj.lEuo9u3w1gXN1o2Ye.YVUbIJgkEd.2vMJrOAIPQMaWQ3DUo6', id
FROM roles
WHERE name = 'ROLE_ADMIN';
