ALTER TABLE aktivitetsplikt_brev ADD COLUMN ar INT;
UPDATE aktivitetsplikt_brev SET ar = EXTRACT(YEAR FROM gjeldende_ar)::INT;