
create table companies (
    id          bigint not null auto_increment,
    name        varchar(255) not null,
    created_at  datetime(6) not null,
    primary key (id)
) engine=InnoDB;

create table users (
    id             bigint not null auto_increment,
    company_id     bigint not null,
    username       varchar(255) not null,
    password_hash  varchar(255) not null,
    name           varchar(255) not null,
    role           enum ('ADMIN','USER') not null,
    created_at     datetime(6) not null,
    primary key (id)
) engine=InnoDB;

create table tickets (
    id            bigint not null auto_increment,
    company_id    bigint not null,
    requester_id  bigint not null,
    assignee_id   bigint,
    title         varchar(255) not null,
    description   varchar(2000) not null,
    status        enum ('CLOSED','IN_PROGRESS','OPEN','RESOLVED') not null,
    priority      enum ('HIGH','LOW','MEDIUM') not null,
    created_at    datetime(6) not null,
    updated_at    datetime(6) not null,
    primary key (id)
) engine=InnoDB;

create table ticket_comments (
    id          bigint not null auto_increment,
    ticket_id   bigint not null,
    author_id   bigint not null,
    message     varchar(1000) not null,
    created_at  datetime(6) not null,
    primary key (id)
) engine=InnoDB;

alter table users add constraint uk_users_username unique (username);

create index idx_user_company_role     on users (company_id, role);
create index idx_ticket_company_status on tickets (company_id, status, created_at);
create index idx_ticket_assignee       on tickets (assignee_id);
create index idx_ticket_requester      on tickets (requester_id);
create index idx_comment_ticket        on ticket_comments (ticket_id, created_at);

alter table users           add constraint fk_user_company       foreign key (company_id)   references companies (id);
alter table tickets         add constraint fk_ticket_company     foreign key (company_id)   references companies (id);
alter table tickets         add constraint fk_ticket_requester   foreign key (requester_id) references users (id);
alter table tickets         add constraint fk_ticket_assignee    foreign key (assignee_id)  references users (id);
alter table ticket_comments add constraint fk_comment_ticket     foreign key (ticket_id)    references tickets (id);
alter table ticket_comments add constraint fk_comment_author     foreign key (author_id)    references users (id);
