# language: no
# encoding: UTF-8

Egenskap: Ugyldig opphørsdato

  Scenario: Opphørsdato etter forrige andel sin opphørsdato er ikke gyldig

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 700   |
      | 2            | 03.2021  | 03.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres kastes exception

