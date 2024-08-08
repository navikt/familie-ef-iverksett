# language: no
# encoding: UTF-8

Egenskap: Opphør etter opphør


  Scenario: Opphør etter opphør skal peke til siste andelen i kjeden

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 700   |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 1            | 03.2021  | 03.2021  | 700   |

      | 2            | 01.2021  | 01.2021  | 700   |
      | 2            | 02.2021  | 02.2021  | 700   |

      | 3            | 01.2021  | 01.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Type |
      | 1            | 01.2021  | 01.2021  |             | 700   | NY           | Nei        | 0          |                    | MND  |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 1          | 0                  | MND  |
      | 1            | 03.2021  | 03.2021  |             | 700   | NY           | Nei        | 2          | 1                  | MND  |
      | 2            | 03.2021  | 03.2021  | 03.2021     | 700   | ENDR         | Ja         | 2          | 1                  | MND  |
      | 3            | 03.2021  | 03.2021  | 02.2021     | 700   | ENDR         | Ja         | 2          | 1                  | MND  |

