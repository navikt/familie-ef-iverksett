# language: no
# encoding: UTF-8

Egenskap: Revurdering med opphør bak i tiden, samt ny periode frem i tiden


  Scenario: Revurdering med opphør bak i tiden, samt ny periode frem i tiden

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 1            | 03.2021  | 03.2021  | 800   |
      | 2            | 01.2021  | 01.2021  | 0     |
      | 2            | 04.2021  | 04.2021  | 800   |


    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 1            | 03.2021  | 03.2021  |             | 800   | NY           | Nei        | 2          | 1                  |
      | 2            | 03.2021  | 03.2021  | 01.2021     | 800   | ENDR         | Ja         | 2          | 1                  |
      | 2            | 04.2021  | 04.2021  |             | 800   | ENDR         | Nei        | 3          | 2                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 1          |                    | 1                   |
      | 03.2021  | 03.2021  | 800   | 2          | 1                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 04.2021  | 04.2021  | 800   | 3          | 2                  | 2                   |

