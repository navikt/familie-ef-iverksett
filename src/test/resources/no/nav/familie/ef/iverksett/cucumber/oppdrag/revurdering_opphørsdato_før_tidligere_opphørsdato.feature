# language: no
# encoding: UTF-8

Egenskap: Startdato før tidligere skal sende nytt startdato til oppdrag


  Scenario: Startdato før tidligere skal sende nytt startdato til oppdrag

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 02.2021   |
      | 3            | 01.2021   |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 700   |

    Og følgende tilkjente ytelser uten andel for Overgangsstønad
      | BehandlingId |
      | 2            |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 3            | 02.2021  | 02.2021  | 800   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 03.2021  | 03.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 2            | 03.2021  | 03.2021  | 02.2021     | 700   | ENDR         | Ja         | 0          |                    |
      | 3            | 03.2021  | 03.2021  | 01.2021     | 700   | ENDR         | Ja         | 0          |                    |
      | 3            | 02.2021  | 02.2021  |             | 800   | ENDR         | Nei        | 1          | 0                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 700   | 0          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 800   | 1          | 0                  | 3                   |