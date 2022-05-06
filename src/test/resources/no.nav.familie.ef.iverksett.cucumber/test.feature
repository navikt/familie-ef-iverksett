# language: no
# encoding: UTF-8

Egenskap: Noe

  Scenario: Noe

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 1     |


    Så forvent følgelse tilkjente ytelser for behandling 1 med startdato 01.2019
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 1     |

    Så forvent følgelse tilkjente ytelser for behandling 1
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 01.2021  | 01.2021  | 1     |
