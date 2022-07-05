# language: no
# encoding: UTF-8

Egenskap: Har en periode, legger til en ny periode som er lik den forrige

  Scenario: Har en periode, legger til en ny periode som er lik den forrige

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 03.2021  | 700   |
      | 2            | 02.2021  | 03.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Startdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 03.2021  |           | 700   | NY           | Nei        | 1          |                    |
      | 2            | 03.2021  | 03.2021  |           | 700   | ENDR         | Nei        | 1          |                    |


    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id |
      | 02.2021  | 03.2021  | 700   | 1          |                    |