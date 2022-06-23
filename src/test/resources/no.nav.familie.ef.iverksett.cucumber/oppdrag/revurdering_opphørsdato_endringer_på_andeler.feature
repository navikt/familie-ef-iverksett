# language: no
# encoding: UTF-8

Egenskap: Opphørsdato er den samme som tidligere - skal ikke sende opphørsdato på nytt når det finnes endringer senere i tiden

  Scenario: Opphørsdato er den samme som tidligere - skal ikke sende opphørsdato på nytt når det finnes endringer senere i tiden

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 02.2021   |
      | 3            | 02.2021   |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 700   |
      | 2            | 03.2021  | 03.2021  | 800   |
      | 3            | 03.2021  | 03.2021  | 800   |
      | 3            | 04.2021  | 04.2021  | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 03.2021  | 03.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 700   | ENDR         | Ja         | 1          |                    |
      | 2            | 03.2021  | 03.2021  |             | 800   | ENDR         | Nei        | 2          | 1                  |
      | 3            | 04.2021  | 04.2021  |             | 900   | ENDR         | Nei        | 3          | 2                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 700   | 1          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 800   | 2          | 1                  | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 800   | 2          | 1                  | 2                   |
      | 04.2021  | 04.2021  | 900   | 3          | 2                  | 3                   |

