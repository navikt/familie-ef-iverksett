# language: no
# encoding: UTF-8
Egenskap: Startdato endrer seg men andelene blir de samme

  Scenario: Startdato endrer seg over et år tilbake i tid men andelene blir de samme

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 1            | 03.2021   |
      | 2            | 02.2020   |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 700   |
      | 2            | 03.2021  | 03.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 03.2021  | 03.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 03.2021  | 03.2021  | 02.2020     | 700   | ENDR         | Ja         | 0          |                    |
      | 2            | 03.2021  | 03.2021  |             | 700   | ENDR         | Nei        | 1          | 0                  |
