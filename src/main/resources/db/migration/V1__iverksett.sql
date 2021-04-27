CREATE TABLE Iverksett
(
    id                                       UUID PRIMARY KEY,
    forrigeTilkjentYtelse                    VARCHAR,---?---
    tilkjentYtelse                           VARCHAR,---?---
    fagsakId                                 VARCHAR,
    saksnummer                               VARCHAR,
    behanlingid                              VARCHAR,
    relatertBehandlingId                     VARCHAR,
    kode6eller7                              BOOLEAN,
    tidspunktVedtak                          DATE,
    vilkårsvurderinger                       VARCHAR,---?---
    person_personIdent                       VARCHAR,
    person_aktorId                           VARCHAR,
    barn                                     VARCHAR, ---?---
    behandlingType                           VARCHAR,
    behandlingÅrsak                          VARCHAR,
    behandlingResultat                       VARCHAR,
    vedtak                                   VARCHAR,
    opphørÅrsak                              VARCHAR,
    inntekt                                  VARCHAR,--?--
    inntektsReduksjon                        VARCHAR, ---?--
    aktivitetskrav_aktivitetspliktInntreffer DATE,
    aktivitetskrav_harSagtOppArbeidsforhold  BOOLEAN,
    funksjonellId                            VARCHAR
)

CREATE TABLE Brev
(
    iverksett_id           UUID REFERENCES Iverksett (id),
    journalpostId          VARCHAR,
    brevdata_mottaker      VARCHAR,
    brevdata_saksbehandler VARCHAR,
    brevdata_pdf           BYTEA
)

CREATE TABLE AndelTilkjentYtelse
(
    periodebeløp_utbetaltPerPeriode NUMBER,
    periodebeløp_periodetype        VARCHAR,
    periodebeløp_fraOgMed           DATE,
    periodebeløp_tilOgMed           DATE,
    personIdent                     VARCHAR,
    periodeId                       BIGINT,
    forrigePeriodeId                BIGINT,
    stønadsType                     VARCHAR,


)