# language: no
# encoding: UTF-8

Egenskap: Startdato etter tidligere startdato er ikke gyldig

  Scenario: Startdato etter tidligere startdato er ikke gyldig


    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 02.2021   |
      | 3            | 03.2021   |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception
