# language: no
# encoding: UTF-8

Egenskap: Opphører vedtak med 0-periode, og deretter innvilget ny stønad

  Scenario: Opphører vedtak med 0-periode, og deretter innvilget ny stønad


    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 02.2021   |
      | 3            | 02.2021   |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 800   |
      | 2            | 03.2021  | 03.2021  | 0     |
      | 3            | 04.2021  | 04.2021  | 100   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 03.2021  | 03.2021  |             | 800   | NY           | Nei        | 0          |                    |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 800   | ENDR         | Ja         | 0          |                    |
      | 3            | 04.2021  | 04.2021  |             | 100   | ENDR         | Nei        | 1          | 0                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 800   | 0          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 0     |            |                    |                     |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 04.2021  | 04.2021  | 100   | 1          | 0                  | 3                   |

