# language: no
# encoding: UTF-8

Egenskap: Førstegangsbehandling med 0 beløp


  Scenario: Revurdering med 0 beløp

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 02.2021  | 0     |
      | 2            | 02.2021  | 02.2021  | 0     |
      | 3            | 02.2021  | 02.2021  | 0     |
      | 4            | 02.2021  | 02.2021  | 700   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag uten utbetalingsperiode
      | BehandlingId | Kode endring | Er endring |
      | 1            | NY           | Nei        |
      | 2            | NY           | Nei        |
      | 3            | NY           | Nei        |

    Og forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Startdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 4            | 02.2021  | 02.2021  |           | 700   | NY           | Nei        | 1          |                    |

    Og forvent følgende tilkjente ytelser med tomme andeler for behandling 1 og startdato 02.2021
    Og forvent følgende tilkjente ytelser med tomme andeler for behandling 2 og startdato 02.2021
    Og forvent følgende tilkjente ytelser med tomme andeler for behandling 3 og startdato 02.2021

    Og forvent følgende tilkjente ytelser for behandling 4 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 02.2021  | 700   | 1          |                    | 4                   |