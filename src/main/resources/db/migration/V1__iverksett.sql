CREATE TABLE Iverksett
(
    id                                       UUID PRIMARY KEY,
    fagsakId                                 varchar,
    saksnummer                               varchar,
    behanglingId                              varchar,
    relatertBehandlingId                     varchar,
    kode6eller7                              boolean,
    tidspunktVedtak                          date,
    person_personIdent                       varchar,
    person_aktorId                           varchar,
    behandlingType                           varchar,
    behandlingArsak                          varchar,
    behandlingResultat                       varchar,
    vedtak                                   varchar,
    opphorArsak                              varchar,
    aktivitetskrav_aktivitetspliktInntreffer date,
    aktivitetskrav_harSagtOppArbeidsforhold  boolean,
    funksjonellId                            varchar
);

CREATE TABLE Brev
(
    id                     UUID not null primary key,
    iverksett_id           UUID REFERENCES Iverksett (id),
    journalpostId          varchar,
    brevdata_mottaker      varchar,
    brevdata_saksbehandler varchar,
    brevdata_pdf           BYTEA
);

CREATE TABLE AndelTilkjentYtelse
(
    id                              UUID not null primary key,
    iverksett_id                    UUID REFERENCES Iverksett (id),
    forrigeYtelse                   boolean,
    periodebeløp_utbetaltPerPeriode numeric(19,2),
    periodebeløp_periodetype        varchar,
    periodebeløp_fraOgMed           DATE,
    periodebeløp_tilOgMed           DATE,
    personIdent                     varchar,
    periodeId                       bigint,
    forrigePeriodeId                bigint,
    stønadsType                     varchar
);

create table Vilkarsvurderinger
(
    id                              UUID not null primary key,
    iverksett_id                    UUID REFERENCES Iverksett (id),
    vilkårType                      varchar,
    resultat                        varchar
);

create table Delvilkarsvurdering
(
    id                              UUID not null primary key,
    vilkarsvurdering_id             UUID REFERENCES Vilkarsvurderinger (id),
    resultat                        varchar

);

create table Vurdering
(
    id                              UUID not null primary key,
    delvilkarsvurdering_id          UUID REFERENCES Delvilkarsvurdering (id),
    regelId                         varchar not null,
    svar                            varchar,
    begrunnelse                     varchar
);
create table Barn
(
    id                              UUID not null primary key,
    iverksett_id                    UUID REFERENCES Iverksett (id),
    personIdent                     varchar,
    aktorId                         varchar
);

create table Inntekt
(
    id                              bigint not null primary key,
    iverksett_id                    UUID REFERENCES Iverksett (id),
    utbetaltPerPeriode              bigint,
    periodeType                     varchar,
    fraOgMed                        date,
    tilOgMed                        date,
    inntektstype                    varchar
);

