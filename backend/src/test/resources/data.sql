-- 테스트(H2) 시드. 운영은 Flyway V2 가 담당.
insert into companies (name, created_at) values ('TestCo', current_timestamp);

-- admin1 = admin123 / user1 = user123 (BCrypt)
insert into users (company_id, username, password_hash, name, role, created_at) values
    (1, 'admin1', '$2y$10$d9yEkpjn8E2sXW58p54dYOYQ4/9vU4ML0tBiZXSeNYo2FdBL2lrWy', '관리자', 'ADMIN', current_timestamp),
    (1, 'user1',  '$2y$10$McPqK8zI.IcTIpSjgInnQObdNPOMj/SJu.PUy/nfU9YLR53YWTZ5K', '사용자', 'USER',  current_timestamp);
