# language: no
# encoding: UTF-8

Egenskap: Sekvens

  Scenario: Sekvens

    Gitt følgende startdatoer
      | BehandlingId | Startdato |
      | 2            | 08.2021   |
      | 3            | 08.2021   |
      | 4            | 08.2021   |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 1            | 08.2021  | 11.2021  | 700   |
      | 1            | 12.2021  | 03.2022  | 900   |
      | 1            | 04.2022  | 06.2022  | 1100  |
      | 2            | 09.2021  | 11.2021  | 700   |
      | 2            | 12.2021  | 03.2022  | 900   |
      | 2            | 04.2022  | 05.2022  | 1100  |
      | 3            | 09.2021  | 11.2021  | 700   |
      # avkorter tom, og har en ny periode direkt etterpå
      | 3            | 12.2021  | 01.2022  | 900   |
      | 3            | 02.2022  | 04.2022  | 1200  |


    Og følgende tilkjente ytelser uten andel for Overgangsstønad
      | BehandlingId |
      | 4            |

    Og følgende tilkjente ytelser for Overgangsstønad
      | BehandlingId | Fra dato | Til dato | Beløp |
      | 5            | 07.2021  | 01.2022  | 900   |

    Når lagTilkjentYtelseMedUtbetalingsoppdrag kjøres

    Så forvent følgende utbetalingsoppdrag
      | BehandlingId | Fra dato | Til dato | Opphørsdato | Beløp | Kode endring | Er endring | Periode id | Forrige periode id |
      | 1            | 08.2021  | 11.2021  |             | 700   | NY           | Nei        | 1          |                    |
      | 1            | 12.2021  | 03.2022  |             | 900   | NY           | Nei        | 2          | 1                  |
      | 1            | 04.2022  | 06.2022  |             | 1100  | NY           | Nei        | 3          | 2                  |
      | 2            | 04.2022  | 06.2022  | 08.2021     | 1100  | ENDR         | Ja         | 3          | 2                  |
      | 2            | 09.2021  | 11.2021  |             | 700   | ENDR         | Nei        | 4          | 3                  |
      | 2            | 12.2021  | 03.2022  |             | 900   | ENDR         | Nei        | 5          | 4                  |
      | 2            | 04.2022  | 05.2022  |             | 1100  | ENDR         | Nei        | 6          | 5                  |
      | 3            | 02.2022  | 04.2022  |             | 1200  | ENDR         | Nei        | 7          | 6                  |
      | 4            | 02.2022  | 04.2022  | 09.2021     | 1200  | ENDR         | Ja         | 7          | 6                  |
      | 5            | 07.2021  | 01.2022  |             | 900   | ENDR         | Nei        | 8          | 7                  |


    Og forvent følgende tilkjente ytelser for behandling 1 med startdato 08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 08.2021  | 11.2021  | 700   | 1          |                    | 1                   |
      | 12.2021  | 03.2022  | 900   | 2          | 1                  | 1                   |
      | 04.2022  | 06.2022  | 1100  | 3          | 2                  | 1                   |

    Og forvent følgende tilkjente ytelser for behandling 2 med startdato 08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 09.2021  | 11.2021  | 700   | 4          | 3                  | 2                   |
      | 12.2021  | 03.2022  | 900   | 5          | 4                  | 2                   |
      | 04.2022  | 05.2022  | 1100  | 6          | 5                  | 2                   |

    Og forvent følgende tilkjente ytelser for behandling 3 med startdato 08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 09.2021  | 11.2021  | 700   | 4          | 3                  | 2                   |
      | 12.2021  | 01.2022  | 900   | 5          | 4                  | 2                   |
      | 02.2022  | 04.2022  | 1200  | 7          | 6                  | 3                   |

    Og forvent følgende tilkjente ytelser for behandling 4 med startdato 08.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |

    Og forvent følgende tilkjente ytelser for behandling 5 med startdato 07.2021
      | Fra dato | Til dato | Beløp | Periode id | Forrige periode id | Kilde behandling id |
      | 07.2021  | 01.2022  | 900   | 8          | 7                  | 5                   |