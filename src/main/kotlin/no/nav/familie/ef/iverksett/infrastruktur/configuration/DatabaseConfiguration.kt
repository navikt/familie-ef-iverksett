package no.nav.familie.ef.iverksett.infrastruktur.configuration

import no.nav.familie.ef.iverksett.brev.domain.Brevmottakere
import no.nav.familie.ef.iverksett.brev.domain.DistribuerBrevResultatMap
import no.nav.familie.ef.iverksett.brev.domain.JournalpostResultatMap
import no.nav.familie.ef.iverksett.iverksetting.domene.Fagsakdetaljer
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettBarnetilsyn
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettData
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettOvergangsstønad
import no.nav.familie.ef.iverksett.iverksetting.domene.IverksettSkolepenger
import no.nav.familie.ef.iverksett.iverksetting.domene.OppdragResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilbakekrevingResultat
import no.nav.familie.ef.iverksett.iverksetting.domene.TilkjentYtelse
import no.nav.familie.eksterne.kontrakter.saksstatistikk.ef.BehandlingDVH
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.prosessering.PropertiesWrapperTilStringConverter
import no.nav.familie.prosessering.StringTilPropertiesWrapperConverter
import org.postgresql.util.PGobject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.Date
import java.time.LocalDate
import java.time.Year
import javax.sql.DataSource

@Configuration
@EnableJdbcRepositories("no.nav.familie")
class DatabaseConfiguration : AbstractJdbcConfiguration() {
    @Bean
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                PropertiesWrapperTilStringConverter(),
                StringTilPropertiesWrapperConverter(),
                BehandlingDVHTilStringConverter(),
                StringTilBehandlingDVHConverter(),
                TilkjentYtelseTilPGobjectConverter(),
                PGobjectTilTilkjentYtelseConverter(),
                OppdragResultatTilPGobjectConverter(),
                PGobjectTilOppdragResultatConverter(),
                JournalpostResultatMapTilPGobjectConverter(),
                PGobjectTilJournalpostResultatMapConverter(),
                VedtaksbrevResultatMapTilPGobjectConverter(),
                PGobjectTilVedtaksbrevResultatMapConverter(),
                TilbakekrevingResultatTilPGobjectConverter(),
                PGobjectTilTilbakekrevingResultatConverter(),
                IverksettDataTilPGobjectConverter(),
                PGobjectConverterTilIverksettData(),
                BrevmottakereTilStringConverter(),
                StringTilBrevmottakereConverter(),
                YearTilLocalDateConverter(),
                LocalDateTilYearConverter(),
            ),
        )

    open class DomainTilPGobjectConverter<T : Any> : Converter<T, PGobject> {
        override fun convert(data: T): PGobject =
            PGobject().apply {
                type = "json"
                value = jsonMapper.writeValueAsString(data)
            }
    }

    @WritingConverter
    class IverksettDataTilPGobjectConverter : DomainTilPGobjectConverter<IverksettData>()

    @ReadingConverter
    class PGobjectConverterTilIverksettData : Converter<PGobject, IverksettData> {
        override fun convert(pGobject: PGobject): IverksettData {
            val fagsakNode = jsonMapper.readTree(pGobject.value).get("fagsak")
            val fagsakdetaljer: Fagsakdetaljer = jsonMapper.treeToValue(fagsakNode, Fagsakdetaljer::class.java)
            return when (fagsakdetaljer.stønadstype) {
                StønadType.BARNETILSYN -> {
                    jsonMapper.readValue(pGobject.value, IverksettBarnetilsyn::class.java)
                }

                StønadType.OVERGANGSSTØNAD -> {
                    jsonMapper.readValue(
                        pGobject.value,
                        IverksettOvergangsstønad::class.java,
                    )
                }

                StønadType.SKOLEPENGER -> {
                    jsonMapper.readValue(pGobject.value, IverksettSkolepenger::class.java)
                }
            }
        }
    }

    @WritingConverter
    class TilkjentYtelseTilPGobjectConverter : DomainTilPGobjectConverter<TilkjentYtelse>()

    @ReadingConverter
    class PGobjectTilTilkjentYtelseConverter : Converter<PGobject, TilkjentYtelse> {
        override fun convert(pGobject: PGobject): TilkjentYtelse = jsonMapper.readValue(pGobject.value!!, TilkjentYtelse::class.java)
    }

    @WritingConverter
    class OppdragResultatTilPGobjectConverter : DomainTilPGobjectConverter<OppdragResultat>()

    @ReadingConverter
    class PGobjectTilOppdragResultatConverter : Converter<PGobject, OppdragResultat> {
        override fun convert(pGobject: PGobject): OppdragResultat = jsonMapper.readValue(pGobject.value!!, OppdragResultat::class.java)
    }

    @WritingConverter
    class JournalpostResultatMapTilPGobjectConverter : DomainTilPGobjectConverter<JournalpostResultatMap>()

    @ReadingConverter
    class PGobjectTilJournalpostResultatMapConverter : Converter<PGobject, JournalpostResultatMap> {
        override fun convert(pGobject: PGobject): JournalpostResultatMap = jsonMapper.readValue(pGobject.value!!, JournalpostResultatMap::class.java)
    }

    @WritingConverter
    class VedtaksbrevResultatMapTilPGobjectConverter : DomainTilPGobjectConverter<DistribuerBrevResultatMap>()

    @ReadingConverter
    class PGobjectTilVedtaksbrevResultatMapConverter : Converter<PGobject, DistribuerBrevResultatMap> {
        override fun convert(pGobject: PGobject): DistribuerBrevResultatMap = jsonMapper.readValue(pGobject.value!!, DistribuerBrevResultatMap::class.java)
    }

    @WritingConverter
    class TilbakekrevingResultatTilPGobjectConverter : DomainTilPGobjectConverter<TilbakekrevingResultat>()

    @ReadingConverter
    class PGobjectTilTilbakekrevingResultatConverter : Converter<PGobject, TilbakekrevingResultat> {
        override fun convert(pGobject: PGobject): TilbakekrevingResultat = jsonMapper.readValue(pGobject.value!!, TilbakekrevingResultat::class.java)
    }

    @WritingConverter
    class BehandlingDVHTilStringConverter : Converter<BehandlingDVH, PGobject> {
        override fun convert(utbetalingsoppdrag: BehandlingDVH): PGobject =
            PGobject().apply {
                type = "json"
                value = jsonMapper.writeValueAsString(utbetalingsoppdrag)
            }
    }

    @ReadingConverter
    class StringTilBehandlingDVHConverter : Converter<PGobject, BehandlingDVH> {
        override fun convert(pgObject: PGobject): BehandlingDVH = jsonMapper.readValue(pgObject.value!!, BehandlingDVH::class.java)
    }

    @WritingConverter
    class BrevmottakereTilStringConverter : Converter<Brevmottakere, PGobject> {
        override fun convert(data: Brevmottakere): PGobject =
            PGobject().apply {
                type = "json"
                value = jsonMapper.writeValueAsString(data)
            }
    }

    @ReadingConverter
    class StringTilBrevmottakereConverter : Converter<PGobject, Brevmottakere> {
        override fun convert(pgObject: PGobject): Brevmottakere = jsonMapper.readValue(pgObject.value!!, Brevmottakere::class.java)
    }

    @WritingConverter
    class YearTilLocalDateConverter : Converter<Year, LocalDate> {
        override fun convert(year: Year): LocalDate = year.atDay(1)
    }

    @ReadingConverter
    class LocalDateTilYearConverter : Converter<Date, Year> {
        override fun convert(date: Date): Year = Year.from(date.toLocalDate())
    }
}
