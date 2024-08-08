# language: no
# encoding: UTF-8

Egenskap: En enkel behandling

  Scenario: Startdato frem i tiden er ikke gyldig

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 1            | 01.2022   |

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 1     |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception


  Scenario: Startdato samme dato som første andelen

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 1            | 01.2021   |

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 1     |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Type |
      | 1            | 01.2021  | 01.2021  |             | 1     | NY           | Nei        | 0          |                    | MND  |

    Så forvent følgende tilkjente ytelser for behandling 1 med startdato 01.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id |
      | 01.2021  | 01.2021  | 1     | 0          |                    |
