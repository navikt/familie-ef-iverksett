# familie-ef-iverksett
App som tilbyr iverksettingstjenester av stønadene for enslige forsørgere.

## Swagger
http://localhost:8094/swagger-ui/index.html

## Bygging lokalt
Appen kjører på JRE 11. Bygging gjøres ved å kjøre `mvn clean install`.

### Autentisering lokalt
Dersom man vil gjøre autentiserte kall mot andre tjenester eller vil kjøre applikasjonen sammen med frontend, må man sette opp følgende miljø-variabler:

#### Client id & client secret
secret kan hentes fra cluster med
`kubectl -n teamfamilie get secret azuread-familie-ef-iverksett-lokal -o json | jq '.data | map_values(@base64d)'`

* `AZURE_APP_CLIENT_ID` (fra secret)
* `AZURE_APP_CLIENT_SECRET` (fra secret)
* Scope for den aktuelle tjenesten (`FAMILIE_INTEGRASJONER_SCOPE`, `FAMILIE_OPPDRAG_SCOPE`, ...)

Legges inn under ApplicationLocal -> Edit Configurations -> Environment Variables.

### Kjøring med in-memory-database
For å kjøre opp appen lokalt, kan en kjøre `ApplicationLocal`.

Appen starter da opp med en in Postgres-database via docker og er da tilgjengelig under `localhost:8094`.
For å aksesseres databasen kan du sjekke loggen og bruker/passord i DbContainerInitializer

### GCP
GCP bruker secrets i stedet for vault.
Anbefaler å bruke [modify-secrets](https://github.com/rajatjindal/kubectl-modify-secret)

#### Database
[Nais doc - Postgres](https://doc.nais.io/persistence/postgres/)

[Nais doc - Responsibilities](https://doc.nais.io/persistence/responsibilities/)
* `brew tap tclass/cloud_sql_proxy`
* `brew install cloud_sql_proxy`

1h temp token
* `gcloud projects add-iam-policy-binding familie-ef-iverksett --member=user:<FIRSTNAME>.<LASTNAME>@nav.no --role=roles/cloudsql.instanceUser --condition="expression=request.time < timestamp('$(date -v '+1H' -u +'%Y-%m-%dT%H:%M:%SZ')'),title=temp_access"`

## Kafka
Topic er opprettet i Aiven og GCP, men den kan også nås fra on-prem. Konfigurasjonen av topic finnes i `topic-dev.yaml` Dersom endringer gjøres på topic, må ny konfigurasjon merges til master.
Etter merge til master må workflow `Deploy kafka topics` kjøres for at endringene skal tre i kraft. 
For å se og verifisere konfigurasjon til gitt topic kan kommandoen `kubectl describe topic teamfamilie.<topic> -n=teamfamilie` kjøres.

### Debugging og lesing fra kø med Kafkacat
Det er mulig å se hva som ligger på kø med Kafkacat uten å lage en egen applikasjon for både dev og prod.
Kjør kommando `kafkacat -F <configFile.config> -C -t teamfamilie.<topicnavn> -o -1 -e` for å lese nyeste melding på topic. 
Se dokumentasjon på kafkacat for å modifisere til å f.eks. se melding på gitt offset.
Installasjon av kafkacat og oppsett av config-fil er dokumentert i familie-repoet under utvikling. 

## Produksjonssetting
Applikasjonen vil deployes til produksjon ved ny commit på master-branchen. Det er dermed tilstrekkelig å merge PR for å trigge produksjonsbygget.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes til:

* Mattis Janitz, `mattis.janitz@nav.no`

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-familie.

## Kode generert av GitHub Copilot

Dette repoet bruker GitHub Copilot til å generere kode.