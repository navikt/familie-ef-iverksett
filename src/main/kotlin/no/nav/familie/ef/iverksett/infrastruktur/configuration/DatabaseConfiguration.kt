package no.nav.familie.ef.iverksett.infrastruktur.configuration

import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
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
import no.nav.familie.kontrakter.felles.objectMapper
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
    fun namedParameterJdbcTemplate(dataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
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
    }

    open class DomainTilPGobjectConverter<T : Any> : Converter<T, PGobject> {

        override fun convert(data: T): PGobject =
            PGobject().apply {
                type = "json"
                value = objectMapper.writeValueAsString(data)
            }
    }

    @WritingConverter
    class IverksettDataTilPGobjectConverter : DomainTilPGobjectConverter<IverksettData>()

    @ReadingConverter
    class PGobjectConverterTilIverksettData : Converter<PGobject, IverksettData> {

        override fun convert(pGobject: PGobject): IverksettData {
            val fagsakNode = objectMapper.readTree(pGobject.value).findValue("fagsak")
            val fagsakdetaljer: Fagsakdetaljer = objectMapper.treeToValue(fagsakNode)
            return when (fagsakdetaljer.stønadstype) {
                StønadType.BARNETILSYN -> objectMapper.readValue(pGobject.value, IverksettBarnetilsyn::class.java)
                StønadType.OVERGANGSSTØNAD -> objectMapper.readValue(pGobject.value, IverksettOvergangsstønad::class.java)
                StønadType.SKOLEPENGER -> objectMapper.readValue(pGobject.value, IverksettSkolepenger::class.java)
            }
        }
    }

    @WritingConverter
    class TilkjentYtelseTilPGobjectConverter : DomainTilPGobjectConverter<TilkjentYtelse>()

    @ReadingConverter
    class PGobjectTilTilkjentYtelseConverter : Converter<PGobject, TilkjentYtelse> {

        override fun convert(pGobject: PGobject): TilkjentYtelse {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class OppdragResultatTilPGobjectConverter : DomainTilPGobjectConverter<OppdragResultat>()

    @ReadingConverter
    class PGobjectTilOppdragResultatConverter : Converter<PGobject, OppdragResultat> {

        override fun convert(pGobject: PGobject): OppdragResultat {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class JournalpostResultatMapTilPGobjectConverter : DomainTilPGobjectConverter<JournalpostResultatMap>()

    @ReadingConverter
    class PGobjectTilJournalpostResultatMapConverter : Converter<PGobject, JournalpostResultatMap> {

        override fun convert(pGobject: PGobject): JournalpostResultatMap {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class VedtaksbrevResultatMapTilPGobjectConverter : DomainTilPGobjectConverter<DistribuerBrevResultatMap>()

    @ReadingConverter
    class PGobjectTilVedtaksbrevResultatMapConverter : Converter<PGobject, DistribuerBrevResultatMap> {

        override fun convert(pGobject: PGobject): DistribuerBrevResultatMap {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class TilbakekrevingResultatTilPGobjectConverter : DomainTilPGobjectConverter<TilbakekrevingResultat>()

    @ReadingConverter
    class PGobjectTilTilbakekrevingResultatConverter : Converter<PGobject, TilbakekrevingResultat> {

        override fun convert(pGobject: PGobject): TilbakekrevingResultat {
            return objectMapper.readValue(pGobject.value!!)
        }
    }

    @WritingConverter
    class BehandlingDVHTilStringConverter : Converter<BehandlingDVH, PGobject> {

        override fun convert(utbetalingsoppdrag: BehandlingDVH): PGobject =
            PGobject().apply {
                type = "json"
                value = objectMapper.writeValueAsString(utbetalingsoppdrag)
            }
    }

    @ReadingConverter
    class StringTilBehandlingDVHConverter : Converter<PGobject, BehandlingDVH> {

        override fun convert(pgObject: PGobject): BehandlingDVH {
            return objectMapper.readValue(pgObject.value!!)
        }
    }

    @WritingConverter
    class BrevmottakereTilStringConverter : Converter<Brevmottakere, PGobject> {

        override fun convert(data: Brevmottakere): PGobject =
            PGobject().apply {
                type = "json"
                value = objectMapper.writeValueAsString(data)
            }
    }

    @ReadingConverter
    class StringTilBrevmottakereConverter : Converter<PGobject, Brevmottakere> {

        override fun convert(pgObject: PGobject): Brevmottakere {
            return objectMapper.readValue(pgObject.value!!)
        }
    }

    @WritingConverter
    class YearTilLocalDateConverter : Converter<Year, LocalDate> {

        override fun convert(year: Year): LocalDate {
            return year.atDay(1)
        }
    }

    @ReadingConverter
    class LocalDateTilYearConverter : Converter<Date, Year> {

        override fun convert(date: Date): Year {
            return Year.from(date.toLocalDate())
        }
    }
}
