# language: no
# encoding: UTF-8

Egenskap: Revurdering frem i tid

  Scenario: Revurdering frem i tid med 0-beløp

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 2            | 02.2021  | 03.2021  | 0     |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 2            | 02.2021  | 02.2021  | 02.2021     | 700   | ENDR         | Ja         | 1          |                    |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 1          |                    | 1                   |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      |          |          | 0     | 1          |                    | 2                   |

  Scenario: Revurdering frem i tid med beløp

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 02.2021   |

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp | Start dato |
      | 1            | 02.2021  | 02.2021  | 700   |            |
      | 2            | 03.2021  | 03.2021  | 600   |            |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 2            | 02.2021  | 02.2021  | 02.2021     | 700   | ENDR         | Ja         | 1          |                    |
      | 2            | 03.2021  | 03.2021  |             | 600   | ENDR         | Nei        | 2          | 1                  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 1          |                    | 1                   |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 600   | 2          | 1                  | 2                   |
