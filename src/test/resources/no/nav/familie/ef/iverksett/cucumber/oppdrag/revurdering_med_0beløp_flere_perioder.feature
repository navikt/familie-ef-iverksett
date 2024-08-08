# language: no
# encoding: UTF-8

Egenskap: Revurdering med 0 beløp beholder periodeId når man har flere perioder


  Scenario: Revurdering med 0 beløp beholder periodeId når man har flere perioder

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 1            | 03.2021  | 03.2021  | 800   |
      | 2            | 02.2021  | 03.2021  | 0     |
      | 3            | 02.2021  | 02.2021  | 0     |
      | 4            | 02.2021  | 02.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 03.2021  | 03.2021  |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 800   | ENDR         | Ja         | 1          | 0                  |
      | 4            | 02.2021  | 02.2021  |             | 700   | ENDR         | Nei        | 2          | 1                  |


    Og forvent følgende utbetalingsoppdrag uten utbetalingsperiode
      | BehandlingId | Kode endring | Er endring |
      | 3            | ENDR         | Ja         |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 0          |                    | 1                   |
      | 03.2021  | 03.2021  | 800   | 1          | 0                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 03.2021  | 0     |            |                    |                     |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 0     |            |                    |                     |

    Og forvent følgende tilkjente ytelser for behandling 4 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 2          | 1                  | 4                   |


  Scenario: Revurdering med 0 beløp beholder periodeId når man har flere perioder, men også endring i tidligere beløp

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 1            | 03.2021  | 03.2021  | 800   |
      | 2            | 02.2021  | 02.2021  | 0     |
      | 2            | 03.2021  | 03.2021  | 800   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 03.2021  | 03.2021  |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 800   | ENDR         | Ja         | 1          | 0                  |
      | 2            | 03.2021  | 03.2021  |             | 800   | ENDR         | Nei        | 2          | 1                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 0          |                    | 1                   |
      | 03.2021  | 03.2021  | 800   | 1          | 0                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 0     |            |                    |                     |
      | 03.2021  | 03.2021  | 800   | 2          | 1                  | 2                   |

  Scenario: Revurdering med 0 beløp beholder periodeId når man har flere perioder, før tidligere periode

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 1            | 03.2021  | 03.2021  | 800   |
      | 2            | 02.2021  | 02.2021  | 0     |
      | 2            | 03.2021  | 03.2021  | 800   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 02.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 03.2021  | 03.2021  |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 800   | ENDR         | Ja         | 1          | 0                  |
      | 2            | 03.2021  | 03.2021  |             | 800   | ENDR         | Nei        | 2          | 1                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 0          |                    | 1                   |
      | 03.2021  | 03.2021  | 800   | 1          | 0                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 0     |            |                    |                     |
      | 03.2021  | 03.2021  | 800   | 2          | 1                  | 2                   |
