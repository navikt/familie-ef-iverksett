UPDATE iverksett SET data =
(select regexp_replace(data::text, '(.*)(vedtaksdato":")(\d+-\d+-\d+)(.*)','\1vedtakstidspunkt":"\3T00:00:00\4')::json from iverksett);
