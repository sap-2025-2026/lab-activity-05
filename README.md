#### Software Architecture and Platforms - a.y. 2025-2026

## Lab Activity #05-20251024  

v1.0.0-20251024

- TTT Game Server case study 
  - An hexagonal/ports-&-adapters architecture, integrating the domain model/business logic devised using DDD in previous lab activity, in a modular monolith style
    - `ttt_game_service.domain`  
    - `ttt_game_service.application`  
    - `ttt_game_service.infrastructure`  
  - Using Postman for interacting with the service and do some tests
  - About validation and arch tests
    - User story (registration feature) and acceptance scenarios in Gherkin
      - `src/test/resources/ttt_game_service_tests/registration.feature`
    - Test class based on Cucumber + TTT Game Service business logic
      - `src/test/java/ttt_game_service_tests` 
      - `src/test/java/ttt_game_service_tests.steps`
  - Example of ArchUnit tests
    - checking hexagonal style
      - `src/test/java/ttt_game_service_tests/ArchitectureTests`

- Next step: decomposing the modula monolith using microservices






  

