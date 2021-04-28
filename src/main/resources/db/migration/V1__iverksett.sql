create table iverksett
(
    behandlingid                             UUID not null PRIMARY KEY,
    iverksettJson                            json,
    versjon                                  varchar
);

create table brev
(
  behandlingid uuid references Iverksett(behandlingid),
  journalpostId varchar,
  pdf           bytea
);

