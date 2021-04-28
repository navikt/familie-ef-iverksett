create table Iverksett
(
    behandlingid                             UUID not null PRIMARY KEY,
    iverksett                                json,
    versjon                                  varchar
);

create table Brev
(
  behandlingid uuid references Iverksett(behandlingid),
  journalpostId varchar,
  pdf           bytea
);

