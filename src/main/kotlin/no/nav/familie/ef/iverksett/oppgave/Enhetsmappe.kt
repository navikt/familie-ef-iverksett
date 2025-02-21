package no.nav.familie.ef.iverksett.oppgave

enum class Enhetsmappe(
    val dev: String,
    val prod: String,
) {
    REVURDERING("41 - Revurdering", "41 Revurdering"),
    SELVSTENDIG_NÆRINGSDRIVENDE("61 - Selvstendig næringsdrivende", "61 Selvstendig næringsdrivende"),
}
