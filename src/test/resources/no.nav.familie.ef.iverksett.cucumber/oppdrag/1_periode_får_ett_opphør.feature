# language: no
# encoding: UTF-8

Egenskap: Opphør

  Scenario: Har en periode og får ett opphør

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 05.2021  | 700   |
      | 2            | 02.2021  | 05.2021  | 0     |


    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Type |
      | 1            | 02.2021  | 05.2021  |             | 700   | NY           | Nei        | 1          |                    | MND  |
      | 2            | 02.2021  | 05.2021  | 02.2021     | 700   | ENDR         | Ja         | 1          |                    | MND  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id |
      | 02.2021  | 05.2021  | 700   | 1          |                    |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id |
      |          |          | 0     | 1          |                    |