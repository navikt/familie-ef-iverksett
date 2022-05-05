package no.nav.familie.ef.iverksett.arena

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.iverksett.util.opprettIverksettOvergangsstønad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.jms.core.JmsTemplate
import java.time.format.DateTimeFormatter
import java.util.UUID

class VedtakhendelseProducerTest {

    private var jmsTemplate: JmsTemplate = mockk(relaxed = true)
    private val vedtakhendelseProducer = VedtakhendelseProducer(jmsTemplate)

    @Test
    fun testVedtakhendelseProducer() {
        val vedtakHendelseXmlSlot = slot<String>()
        every { jmsTemplate.convertAndSend(capture(vedtakHendelseXmlSlot)) } just Runs

        val iverksett = opprettIverksettOvergangsstønad(UUID.randomUUID())
        val vedtakHendelser = mapIverksettTilVedtakHendelser(iverksett, "a123")
        vedtakhendelseProducer.produce(vedtakHendelser)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val forventetXML = forventetXML(vedtakHendelser.hendelsesTidspunkt.format(formatter))
        assertThat(vedtakHendelseXmlSlot.captured).isEqualTo(forventetXML)
    }
}

private fun forventetXML(hendelsesTidspunkt: String): String {
    return """
        <vedtakHendelser xmlns="http://nav.no/melding/virksomhet/vedtakHendelser/v1/vedtakHendelser">
            <aktoerID xmlns="">a123</aktoerID>
            <avslutningsstatus xmlns="">innvilget</avslutningsstatus>
            <behandlingstema xmlns="">ab0071</behandlingstema>
            <hendelsesprodusentREF xmlns="">EF</hendelsesprodusentREF>
            <applikasjonSakREF xmlns="">1</applikasjonSakREF>
            <hendelsesTidspunkt xmlns="">${hendelsesTidspunkt}</hendelsesTidspunkt>
        </vedtakHendelser>
    """.trim()
            .replace("\n", "")
            .replace("""> *<""".toRegex(), "><")

}