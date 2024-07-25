Please create a Spring Boot application with Java any version since 11, preferably
use Gradle for building the project. Implement REST API where a customer can
execute next actions:
- get a list of currencies used in the project;
- get exchange rates for a currency;
- add new currency for getting exchange rates.
  
Requirements to the Test task:
- initially a currency list is empty; you should receive exchange rates from
external public sources implementing integration; it can be any public available
source, e.g. fixer.io, exchangeratesapi.io, openexchangerates.org,
currencylayer.com, etc.;
- receiving of exchange rates should be scheduled (e.g. every hour); they
should be logged in the database and stored in memory Map; API gets data
from the Map;
- for a database use PostgreSQL, manage DB schema with Liquibase (any
format - xml, sql, yaml, json); data management should be provided with
Spring Data JPA or Spring Data JDBC;
- an application can be started in the native environment (without containers);
PostgreSQL and any other possible services should be started in Docker
containers; Docker containers should be described with docker-compose file;
- mostly models, controllers and services should be covered by unit and
functional tests, preferably JUnit 5 and Spring Test Framework.

Comments:
- if you have any difficulties with currency switching in a chosen public
exchange source, please use a single currency passing off it as another;
- don’t implement error handling, use runtime exceptions with errors’
descriptions;
- you can skip an input and output models validation;
- don’t use Spring Security Framework.
