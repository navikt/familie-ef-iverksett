ALTER TABLE karakterutskrift_brev ADD CONSTRAINT fagsakIdBrevtypeGjeldendeAar UNIQUE (ekstern_fagsak_id, brevtype, gjeldende_ar);
