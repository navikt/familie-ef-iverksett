# language: no
# encoding: UTF-8

Egenskap: Revurdering tilbake i tid

  Scenario: Revurdering tilbake i tid før vi hadde beløp

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 2            | 01.2021  | 01.2021  | 0     |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 02.2021  | 02.2021  | 01.2021     | 700   | ENDR         | Ja         | 0          |                    |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 0          |                    | 1                   |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 01.2021  | 01.2021  | 0     |            |                    |                     |


  Scenario: Revurdering tilbake i tid med beløp

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 2            | 01.2021  | 01.2021  | 600   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 01.2021  | 01.2021  |             | 600   | ENDR         | Nei        | 1          | 0                  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 0          |                    | 1                   |

    Så forvent følgende tilkjente ytelser for behandling 2 med startdato 01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 01.2021  | 01.2021  | 600   | 1          | 0                  | 2                   |