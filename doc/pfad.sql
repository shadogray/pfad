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

create index booking_has_activity_idx on booking (activity_id);
create index booking_has_member_idx on booking (member_id);
create index booking_has_activity_and_menber_idx on booking (activity_id, member_id);
create index member_has_trupp on member (trupp_id);
create index member_has_vollzahler on member (vollzahler_id);
create index member_has_function on member (funktionen_id);
create index payment_has_payer on payment (payer_id);
