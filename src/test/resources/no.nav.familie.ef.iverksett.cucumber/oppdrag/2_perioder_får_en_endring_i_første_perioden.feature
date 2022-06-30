# language: no
# encoding: UTF-8

Egenskap: 2 perioder med endring på en av periodene

  Scenario: Har 2 perioder og får en endring i første periode

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 04.2021  | 700   |
      | 1            | 05.2021  | 07.2021  | 900   |
      | 2            | 02.2021  | 03.2021  | 700   |
      | 2            | 05.2021  | 07.2021  | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Startdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 04.2021  |           | 700   | NY           | Nei        | 1          |                    |
      | 1            | 05.2021  | 07.2021  |           | 900   | NY           | Nei        | 2          | 1                  |
      | 2            | 05.2021  | 07.2021  | 02.2021   | 900   | ENDR         | Ja         | 2          | 1                  |
      | 2            | 02.2021  | 03.2021  |           | 700   | ENDR         | Nei        | 3          | 2                  |
      | 2            | 05.2021  | 07.2021  |           | 900   | ENDR         | Nei        | 4          | 3                  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 1          |                    | 1                   |
      | 05.2021  | 07.2021  | 900   | 2          | 1                  | 1                   |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 03.2021  | 700   | 3          | 2                  | 2                   |
      | 05.2021  | 07.2021  | 900   | 4          | 3                  | 2                   |
