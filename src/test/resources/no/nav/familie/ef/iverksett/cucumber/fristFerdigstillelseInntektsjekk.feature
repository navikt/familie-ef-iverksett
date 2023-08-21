# language: no
# encoding: UTF-8

Egenskap: Ved en førstegangsbehandling kan saksbehandler velge at det automatisk skal opprettes en fremleggsoppgave for sjekk av inntekt 1 år frem i tid.

  Scenariomal: Fremleggsoppgaver skal ikke falle på den 6. i hver måned, 17. og 18. mai, eller i juli eller august

    Gitt følgende vedtaksdato <Vedtaksdato>

    Når lag frist for ferdigstillelse av inntektsjekk

    Så forvent frist satt til <Dato>

    Eksempler:
      | Vedtaksdato | Dato       | Kommentar                                                                            |
      | 01.09.2023  | 01.09.2024 | Ett år frem i tid                                                                    |
      | 21.08.2023  | 21.06.2024 | Skal ikke lande på juli eller august, trekker fra 2 måneder                          |
      | 01.07.2023  | 01.05.2024 | Skal ikke lande på juli eller august, trekker fra 2 måneder                          |
      | 17.05.2023  | 15.05.2024 | Skal ikke lande på 17. mai, trekker fra 2 dager                                      |
      | 18.05.2023  | 16.05.2024 | Skal ikke lande på 18. mai, trekker fra 2 dager                                      |
      | 06.05.2023  | 07.05.2024 | Skal ikke lande på 6. dagen i en måned, skal legge til en dag                        |
      | 06.07.2023  | 07.05.2024 | Skal ikke lande på juli/august og den 6., trekker fra 2 måneder og legger til en dag |
      | 17.07.2023  | 15.05.2024 | Skal ikke lande på juli og den 17/18 mai., trekker fra 2 måneder og to dager         |

