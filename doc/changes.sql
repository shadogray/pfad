2018.08.29: Anmeldungen, die nach Aufnahme nicht im Status 'Mitglied' waren:

select r.id, m.id, r.name, r.vorname, r.member_id, m.id, m.name, m.vorname
    from Registration r inner join Member m on r.name = m.name and r.vorname = m.vorname
where r.status = 'Erstellt'
and m.id = r.member_id;

--update Registration set status = 'Mitglied' where id in (
--    select r.id from Registration r inner join Member m on r.name = m.name and r.vorname = m.vorname
--    where r.status = 'Erstellt'  and m.id = r.member_id
--);
--update registration set status = 'Mitglied' where id in (187,222);

