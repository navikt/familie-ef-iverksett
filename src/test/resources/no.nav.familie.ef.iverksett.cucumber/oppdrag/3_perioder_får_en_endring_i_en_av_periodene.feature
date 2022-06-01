# language: no
# encoding: UTF-8

Egenskap: 3 perioder og får endring i en av periodene


  Scenario: Har 3 perioder og får en endring i den første perioden

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 04.2021  | 700   |
      | 1            | 05.2021  | 07.2021  | 900   |
      | 1            | 08.2021  | 10.2021  | 1000  |
      | 2            | 02.2021  | 04.2021  | 500   |
      | 2            | 05.2021  | 07.2021  | 900   |
      | 2            | 08.2021  | 10.2021  | 1000  |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 04.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 1            | 05.2021  | 07.2021  |             | 900   | NY           | Nei        | 2          | 1                  |
      | 1            | 08.2021  | 10.2021  |             | 1000  | NY           | Nei        | 3          | 2                  |
      | 2            | 08.2021  | 10.2021  | 02.2021     | 1000  | ENDR         | Ja         | 3          | 2                  |
      | 2            | 02.2021  | 04.2021  |             | 500   | ENDR         | Nei        | 4          | 3                  |
      | 2            | 05.2021  | 07.2021  |             | 900   | ENDR         | Nei        | 5          | 4                  |
      | 2            | 08.2021  | 10.2021  |             | 1000  | ENDR         | Nei        | 6          | 5                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 1          |                    | 1                   |
      | 05.2021  | 07.2021  | 900   | 2          | 1                  | 1                   |
      | 08.2021  | 10.2021  | 1000  | 3          | 2                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 500   | 4          | 3                  | 2                   |
      | 05.2021  | 07.2021  | 900   | 5          | 4                  | 2                   |
      | 08.2021  | 10.2021  | 1000  | 6          | 5                  | 2                   |

  Scenario: Har 3 perioder og får en endring i den andre perioden

    Gitt følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 02.2021  | 04.2021  | 700   |
      | 1            | 05.2021  | 07.2021  | 900   |
      | 1            | 08.2021  | 10.2021  | 1000  |
      | 2            | 02.2021  | 04.2021  | 700   |
      | 2            | 05.2021  | 07.2021  | 800   |
      | 2            | 08.2021  | 10.2021  | 1000  |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 02.2021  | 04.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 1            | 05.2021  | 07.2021  |             | 900   | NY           | Nei        | 2          | 1                  |
      | 1            | 08.2021  | 10.2021  |             | 1000  | NY           | Nei        | 3          | 2                  |
      | 2            | 08.2021  | 10.2021  | 05.2021     | 1000  | ENDR         | Ja         | 3          | 2                  |
      | 2            | 05.2021  | 07.2021  |             | 800   | ENDR         | Nei        | 4          | 3                  |
      | 2            | 08.2021  | 10.2021  |             | 1000  | ENDR         | Nei        | 5          | 4                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 1          |                    | 1                   |
      | 05.2021  | 07.2021  | 900   | 2          | 1                  | 1                   |
      | 08.2021  | 10.2021  | 1000  | 3          | 2                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 02.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 02.2021  | 04.2021  | 700   | 1          |                    | 1                   |
      | 05.2021  | 07.2021  | 800   | 4          | 3                  | 2                   |
      | 08.2021  | 10.2021  | 1000  | 5          | 4                  | 2                   |

