drop sequence activity_seq;
create sequence activity_seq start with (select max(id)+1 from activity);
drop sequence payment_seq;
create sequence payment_seq start with (select max(id)+1 from payment);
drop sequence booking_seq;
create sequence booking_seq start with (select max(id)+1 from booking);

alter table booking alter column version set default 0;
alter table payment alter column version set default 0;
alter table member alter column version set default 0;
alter table squad alter column version set default 0;
alter table function alter column version set default 0;