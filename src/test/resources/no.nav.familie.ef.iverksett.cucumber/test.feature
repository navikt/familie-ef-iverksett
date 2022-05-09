# language: no
# encoding: UTF-8

Egenskap: Cucumber oppsett

  Scenario: Enkel behandling

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 1            | 09.2020   |

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 1     |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgelse utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id | Type |
      | 1            | 01.2021  | 01.2021  |             | 1     | NY           | Nei        | 1          |                    | MND  |

    Så forvent følgelse tilkjente ytelser for behandling 1 med startdato 09.2020
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Periodetype |
      | 01.2021  | 01.2021  | 1     | 1          |                    | Måned       |
