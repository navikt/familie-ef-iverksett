package no.nav.familie.ef.iverksett.infrastruktur.json

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VedtakJSONTransformTest {

    @Test
    fun `transformerVedtakJSON forvent like verdier`() {
        val vedtakJSON = IverksettJson(true, "Begrunnelse")
        assertThat(vedtakJSON).isEqualToComparingFieldByField(vedtakJSON.transform())
    }
}