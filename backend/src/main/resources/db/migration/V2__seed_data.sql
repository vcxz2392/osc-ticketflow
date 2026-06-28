-- 로컬 데모 시드. (운영 배포 시 제외/분리)

insert into companies (name, created_at) values
    ('Acme Corp', current_timestamp(6)),
    ('Globex', current_timestamp(6));

-- 사용자 (비밀번호: admin* = admin123 / user* = user123, BCrypt)
insert into users (company_id, username, password_hash, name, role, created_at) values
    (1, 'admin1', '$2y$10$d9yEkpjn8E2sXW58p54dYOYQ4/9vU4ML0tBiZXSeNYo2FdBL2lrWy', '김관리', 'ADMIN', current_timestamp(6)),
    (1, 'user1',  '$2y$10$McPqK8zI.IcTIpSjgInnQObdNPOMj/SJu.PUy/nfU9YLR53YWTZ5K', '이요청', 'USER',  current_timestamp(6)),
    (1, 'user2',  '$2y$10$McPqK8zI.IcTIpSjgInnQObdNPOMj/SJu.PUy/nfU9YLR53YWTZ5K', '박사용', 'USER',  current_timestamp(6)),
    (2, 'admin2', '$2y$10$d9yEkpjn8E2sXW58p54dYOYQ4/9vU4ML0tBiZXSeNYo2FdBL2lrWy', '최담당', 'ADMIN', current_timestamp(6)),
    (2, 'user3',  '$2y$10$McPqK8zI.IcTIpSjgInnQObdNPOMj/SJu.PUy/nfU9YLR53YWTZ5K', '정유저', 'USER',  current_timestamp(6));

-- 샘플 티켓 (Acme Corp)
insert into tickets (company_id, requester_id, assignee_id, title, description, status, priority, created_at, updated_at) values
    (1, 2, 1,    '로그인이 안 됩니다',        '비밀번호 재설정 후에도 로그인 실패합니다.', 'IN_PROGRESS', 'HIGH',   current_timestamp(6), current_timestamp(6)),
    (1, 2, null, '프린터 연결 요청',          '3층 회의실 프린터 연결 부탁드립니다.',       'OPEN',        'LOW',    current_timestamp(6), current_timestamp(6)),
    (1, 3, 1,    'VPN 속도 저하',             '오후부터 VPN 속도가 매우 느립니다.',         'RESOLVED',    'MEDIUM', current_timestamp(6), current_timestamp(6));

insert into ticket_comments (ticket_id, author_id, message, created_at) values
    (1, 1, '확인 중입니다. 계정 잠금 여부 점검하겠습니다.', current_timestamp(6)),
    (3, 1, 'VPN 게이트웨이 재시작으로 해결되었습니다.',     current_timestamp(6));
