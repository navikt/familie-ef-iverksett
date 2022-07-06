# language: no
# encoding: UTF-8

Egenskap: Kan opphøre når man bare har 0-andeler


  Scenario: Kan opphøre når man bare har 0-andeler

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 3            | 01.2021   |

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 700   |
      | 2            | 02.2021  | 04.2021  | 0     |
      | 3            | 02.2021  | 02.2021  | 800   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 03.2021  | 03.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 700   | ENDR         | Ja         | 1          |                    |
      | 3            | 03.2021  | 03.2021  | 01.2021     | 700   | ENDR         | Ja         | 1          |                    |
      | 3            | 02.2021  | 02.2021  |             | 800   | ENDR         | Nei        | 2          | 1                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 700   | 1          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |          |          | 0     | 1          |                    | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 800   | 2          | 1                  | 3                   |