# language: no
# encoding: UTF-8

Egenskap: 2 perioder får en ny periode og en endring

  Scenario: Har 2 perioder og får ny periode og endring i andre periode

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 04.2021  | 700   |
      | 1            | 05.2021  | 07.2021  | 900   |
      | 2            | 02.2021  | 04.2021  | 700   |
      | 2            | 05.2021  | 07.2021  | 900   |
      | 2            | 08.2021  | 09.2021  | 100   |
      | 3            | 02.2021  | 04.2021  | 700   |
      | 3            | 05.2021  | 06.2021  | 900   |
      | 3            | 08.2021  | 09.2021  | 100   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 04.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 05.2021  | 07.2021  |             | 900   | NY           | Nei        | 1          | 0                  |
      | 2            | 08.2021  | 09.2021  |             | 100   | ENDR         | Nei        | 2          | 1                  |
      | 3            | 08.2021  | 09.2021  | 07.2021     | 100   | ENDR         | Ja         | 2          | 1                  |
      | 3            | 08.2021  | 09.2021  |             | 100   | ENDR         | Nei        | 3          | 2                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 0          |                    | 1                   |
      | 05.2021  | 07.2021  | 900   | 1          | 0                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 0          |                    | 1                   |
      | 05.2021  | 07.2021  | 900   | 1          | 0                  | 1                   |
      | 08.2021  | 09.2021  | 100   | 2          | 1                  | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 0          |                    | 1                   |
      | 05.2021  | 06.2021  | 900   | 1          | 0                  | 1                   |
      | 08.2021  | 09.2021  | 100   | 3          | 2                  | 3                   |