ALTER TABLE aktivitetsplikt_brev ADD COLUMN ar INT;
UPDATE aktivitetsplikt_brev SET ar = EXTRACT(YEAR FROM gjeldende_ar)::INT;
ALTER TABLE aktivitetsplikt_brev ALTER COLUMN ar SET NOT NULL;