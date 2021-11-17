UPDATE iverksett SET data = regexp_replace(data::text, '(.*)(vedtaksdato":")(\d+-\d+-\d+)(.*)','\1vedtakstidspunkt":"\3T00:00:00\4')::json;
