# language: no
# encoding: UTF-8

Egenskap: Revurderer og sletter periode 2, revurdererer på nytt og skal da bygge videre fra andre perioden sin periodeId


  Scenario: Revurderer og sletter periode 2, revurdererer på nytt og skal da bygge videre fra andre perioden sin periodeId

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 03.2021  | 03.2021  | 700   |
      | 1            | 04.2021  | 04.2021  | 800   |
      | 2            | 03.2021  | 03.2021  | 700   |
      | 3            | 03.2021  | 03.2021  | 700   |
      | 3            | 04.2021  | 04.2021  | 800   |


    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 03.2021  | 03.2021  |             | 700   | NY           | Nei        | 0          |                    |
      | 1            | 04.2021  | 04.2021  |             | 800   | NY           | Nei        | 1          | 0                  |
      | 2            | 04.2021  | 04.2021  | 04.2021     | 800   | ENDR         | Ja         | 1          | 0                  |
      | 3            | 04.2021  | 04.2021  |             | 800   | ENDR         | Nei        | 2          | 1                  |

    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 700   | 0          |                    | 1                   |
      | 04.2021  | 04.2021  | 800   | 1          | 0                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 700   | 0          |                    | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 03.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 03.2021  | 03.2021  | 700   | 0          |                    | 1                   |
      | 04.2021  | 04.2021  | 800   | 2          | 1                  | 3                   |