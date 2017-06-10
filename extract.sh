#perl -0 -pe 's/vornameKind:\s+(\w+.*)\nnachnameKind:\s+(\w+.*)\ngeburtsdatum:\s+(\w+.*)\nschuleintritt:\s+(\w+.*)\n/$1 $2 $3 $4 $5 $6 $7 $8 $9/g' Registrierungen.txt

#perl -0 -pe 's/vornameKind:\s+(\w+.*)\nnachnameKind:\s+(\w+.*)\ngeburtsdatum:\s+(\w+.*)\nschuleintritt:\s+(\w+.*)\nvornameErziehungsberechtigter:\s+(\w+.*)\nnachnameErziehungsberechtigter:\s+(\w+.*)\nadresse:\s+(\w+.*)( +(\d+))?(\s+(\w+))?\ntelefon:\s+(\w+.*)\nemail:\s+(\w+.*)/$1 $2 $3 $4 $5 $6 $7 $8 $9/g' Registrierungen.txt 


perl -0 -pe 's/Datum: (.*)\n.*\n.*\nvornameKind:\s+(\w+.*)\nnachnameKind:\s+(\w+.*)\ngeburtsdatum:\s+(\w+.*)\nschuleintritt:\s+(\w+.*)\nvornameErziehungsberechtigter:\s+(\w+.*)\nnachnameErziehungsberechtigter:\s+(\w+.*)\nadresse:\s+(\w+.*)\ntelefon:\s+(\w+.*)\nemail:\s+(\w+.*)/$1;$2;$3;$4;$5;$6;$7;$8;$9;$10;$11/g' Registrierungen.txt


30. Mai 2017 15:47:08 MESZ;Alina Valentina;Bauer;27.01.2010;2016;Gergana;Dimova-Bauer;Bahnzeile, 1B;06647992847;gergana.dimova@gmail.com;
23. Mai 2017 16:24:37 MESZ;Lisa;CORKHILL;07.09.2009;2016;Julia;Corkhill-Goettlicher;Gamingerstrasse 24;06767606628;julia.goettlicher@gmx.at;
18. Mai 2017 09:49:29 MESZ;Livia;Kapeller;19.02.2010;2016;Mariella;Klement-Kapeller;Sängerhofgasse 40 d 2512 Tribuswinkel;06769711551;klement-kapeller@aon.at;
06. Mai 2017 13:59:26 MESZ;Lana;Alwani;4.2.2007;2013;Darin;Abara;Leesdorfer Hauptstr. 63, 2500 Baden;068120285078;ruth.forsthuber@gmail.com;
06. Mai 2017 13:57:41 MESZ;Kinan;Alwani;17.07.2011;2017;Darin;Abara;Leesdorfer Hauptstr. 63, 2500 Baden;068120285078;ruth.forsthuber@gmail.com;
02. Mai 2017 13:00:44 MESZ;Laura;Kratochvil;21.08.2013;2019;Thomas;Kratochvil;Josefsthaler Str. 11, 2512 Tribuswinkel;066473024588;thomas@kratochvil.at;
30. April 2017 17:10:16 MESZ;Eileen;Bayram;11.08.2012;2018;Miriam;Bayram;Alzenauerplatz 6/7/1,2511 Pfaffstatten;06763669355;miriam.bayram@gmail.com;
30. April 2017 17:07:10 MESZ;Mikail;Bayram;08.02.2010;2016;Miriam;Bayram;Alzenauerplatz 6/7/1;06763669355;Miriam.bayram@gmail.com;
27. April 2017 10:30:20 MESZ;Amelie;Tahiraj;06.11.2010;2017;Judbina und Masar;Tahiraj;Florianistrasse 3, 2522 Oberwaltersdorf;0676/31 51 952;judbina.tahiraj@noel.gv.at;
19. April 2017 10:42:11 MESZ;Philipp;Wochel;21.04.2008;2014;Sieglinde / Andreas;Strigl / Wochel;Klesheimstraße 41, 2500 Baden;0676 84 77 84 37;sieglinde.strigl@hm.com;
18. April 2017 15:53:56 MESZ;Jakob;Steinkellner;05.06.2010;2016;Elisabeth;Steinkellner;Valeriestraße 12/35;06508518173;elisabethsteinkellner@hotmail.com;
12. April 2017 15:01:21 MESZ;Luis;Wieland;13.05.2016;2022;Barbara;Wieland;Leopold Breinschmid-Gasse 16;06769474774;wieland.babsi@gmail.com;
09. April 2017 18:10:26 MESZ;Leopold;Gubenschek;25.08.2012;2018;Lisa;Gubenschek;Dr herta firnbergggasse 14 2512 oeynhaus;069918112390;L.gubenschek@gmail.com;
09. April 2017 18:09:15 MESZ;Lara-sophie;Gubenschek;27.09.2010;2017;Lisa;Gubenschek;Dr herta firnberggasse 14;069918112390;L.gubenschek@gmail.com;
28. März 2017 16:02:15 MESZ;Theodor;Rollinger;22.08.2016;2022 oder 23;Katharina;Rollinger;J. Kollmannstr. 7/2/34, 2500 Baden;0699/10021700;rollinger@wortspiele.at;
28. Oktober 2016 10:22:41 MESZ;Clemens;Kern;28.04.2011;2017;Bettina;Kern;Gymnasiumstrasse 14, 2500 Baden;0676/5579122;bettinakern313@hotmail.com;
31. Juli 2016 14:32:30 MESZ;Maximilian;Rath;06.04.2012;2018;Rita;Tauscher;Rauheneckg. 22, 2500 Baden;06506775290;rita.tauscher@gmail.com;
