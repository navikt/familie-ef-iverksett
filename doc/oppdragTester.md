# CSV-tester for utbetalingsgeneratorn

## Beskrivelse av kolonner

| Type | Fnr | Oppdrag | Ytelse | LID | Pre-LID | Status oppdrag | Er endring | 2020-01 | 2020-02 | 2020-03 |
| ---- | --- | ------- | ------ | --- | ------- | -------------- | ---------- | ------- | ------- | ------- |

* Type
    * Input: Andeler som vi sender inn som nye andeler inn i utbetalingsgeneratorn
    * Oppdrag: Output av oppdrag, denne inneholder eks LID, Pre-LID, Status oppdrag, er Endring
    * Output: Nye andeler, som blir brukt når man senere gjør en revurdering for å beregne ny kjede
* Fnr: Fnr til personen det gjelder
* Oppdrag
    * `Id` på oppdraget, blir mappet til en UUID, men er ett tall i CSV-testene for å forenkle 
* Ytelse: type ytelse - Denne er egentlige ikke i bruk akkurat nå
* LID
    * Mappes til `periodeId` - tall
    * Økonomi trenger å få en kjede av andeler som peker til hverendre, denne er eks 1,2,3, hvis man då har 2 perioder så får
      første andelen `periodeId=1`, andre andelen `periodeId=2`
* Pre-LID
    * Mappes til `forrigePeriodeId`
    * Hvis man har 2 perioder, så peker ikke første andelen til noe, og er då null, mens den andre andelen peker til første, som
      då er 1 (då `periodeId=1` for første perioden)
* Status oppdrag
    * Kun for `type=Oppdrag`
    * `NY/ENDR`
    * Første andelen i en kjede settes til `NY`, alle andre er `ENDR`
* Er endring
    * Kun for `type=Oppdrag`
    * Beskriver om selve perioden er en endring
    * Ved `true` vil oppdrag sette asksjonskode `ENDR` på linje og ikke referere bakover
* < dato >
    * For hver rad, så må alle beløp være de samme, og de må være i alle kolonner mellom laveste og høyeste måneden.

| 2001-01 | 2001-02 | 2001-03 |
| ------- | ------- | ------- | 
| 200     | 200     | 200     |

Denne sier då att 2001-01 til 2001-03 har personen 200 per måned

### Rader

Hver rad av den samme type representerer en rad for den typen, eks 2 rader med input er 2 andeler (2 perioder) med beløp. En
gruppe av input/output representeres av typene `input`+`oppdrag`+`output`

| Rad | Type    | Gruppe |
| --- | ------- | ------ |
| 1   | Input   | 1      |
| 2   | Oppdrag | 1      |
| 3   | Output  | 1      |
| 4   | Input   | 2      |
| 5   | Oppdrag | 2      |
| 6   | Output  | 2      |

#### Startdato på Input output
* Startdato på tilkjent ytelse settes gjennom å sette x på den måned i input/output som startdato er gjeldende fra
* Hvis man ikke setter startdato så brukes den første perioden for den input/output.
* Hvis man då ikke har en periode, så kaster den feil og man må då sette x i en kolonne som indikerer startdato 

## Kjeder
Når vi sender perioder til økonomi så slår vi de sammen til en kjede, dvs en periode har ett forhold til forrige periode - litt som en `LinkedList`. <br />
Hver periode har en `periodeId`. En periode refererer alltid til forrige periode med `forrigePeriodeId`, hvis det ikke er den første perioden som sendes for personen.

### Opphør
Når vi sender opphør så sender vi med siste perioden i kjeden i utbetalingsoppdraget, samtidig som vi sender med fra hvilket dato vi skal opphøre.

#### 2 perioder der vi opphør/endrer den siste
Input <br />
2001-01 - 2001-01 100kr <br />
2001-02 - 2001-02 100kr <br />
Hvis vi endrer denne i en ny tilkjent ytelse til å kun inneholde første perioden, så kommer vi opprette ett utbetalingsoppdrag med siste perioden. <br />
Hvis vi opphør begge perioder sender vi siste perioden, men med opphør fra 2001-01, som er fra-dato i første perioden.

#### Feilistuasjoner
Hvis man får feil på eks index 2, så betyder det for gruppe 2. Sjekk loggen/diff som gir feil.