# language: no
# encoding: UTF-8

Egenskap: Har en periode, legger til en ny

  Scenario: Har en periode, legger til en ny

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 03.2021  | 700   |
      | 2            | 02.2021  | 03.2021  | 700   |
      | 2            | 04.2021  | 05.2021  | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Startdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Type |
      | 1            | 02.2021  | 03.2021  |           | 700   | NY           | Nei        | 1          |                    | MND  |
      | 2            | 04.2021  | 05.2021  |           | 900   | ENDR         | Nei        | 2          | 1                  | MND  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id |
      | 02.2021  | 03.2021  | 700   | 1          |                    |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id |
      | 02.2021  | 05.2021  | 900   | 2          | 1                  |

