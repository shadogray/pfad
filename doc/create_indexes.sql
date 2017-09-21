create index booking_activity_id_idx on booking (activity_id);
create index booking_member_id_idx on booking (member_id);
create index booking_squad_id_idx on booking (squad_id);

create index member_squad_id_idx on member (trupp_id);
create index member_funktionen_id_idx on member (funktionen_id);
create index member_vollzahler_id_idx on member (vollzahler_id);


create index participation_member_id_idx on participation (member_id);
create index participation_training_id_idx on participation (training_id);

create index payment_payer_id on payment (payer_id);

create index registration_parent_id on registration (parent_id);
create index registration_member_id on registration (member_id);

create index squad_leadermale_id on squad (leadermale_id);
create index squad_leaderfemale_id on squad (leaderfemale_id);

