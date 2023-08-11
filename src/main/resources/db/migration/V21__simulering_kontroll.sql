CREATE TABLE simuleringskontroll (
    behandling_id UUID PRIMARY KEY,
    input JSON NOT NULL,
    resultat JSON NOT NULL
)