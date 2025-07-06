## EatClub coding challenge

This project was created as Amila Chandrasiri's submission to EatClub coding challenge.

Coding challenge can be found at the project root level
here: [EatClub Tech Challenge](./EatClub%20Tech%20Challenge%20-%20Java%20AWS%20-%20v2.pdf)

## Assumptions and decisions

- Project is written primarily in Java targeting v21
- GraalVM plugin is enabled, just because I wanted to mess around with its functionality.
- Project uses embedded Netty server
- Project is dockerised
- Basic spring security is configured, and all requests are authenticated against a single admin
  <br> user specified in [App config file](./src/main/resources/application.properties)

- if a deal doesn't have start/end times, it is assumed the deal is available throughout the
  <br> restaurant opening hours

- Deals with less than 1 qyt available are excluded