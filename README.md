## Upwork Task
Create a simple URL shortener service (UI is optional but will be considered a plus). Plan the implementation
to be suitable for very high volume of traffic and large amounts of data.

The service should be able to:
-  Shorten a given URL and return shortened link;
- Expand a shortened URL into the original one and redirect to the original location.
- Shortened URLs should expire after a configured retention period.

##### Implementation expectations
- Satisfactory implementation should consider aspects of the high volume and/or high load
- Security precautions should be taken for all shortened URLs (it is up to you to make a decisions what
those security precautions are)
- Think how you can optimize the storage
- Consider how to scale your solution horizontally (it's not necessary to implement it, but prepare to
explain this when you present your solution)
- [Optional] Think about monetization aspects of the service

##### Language/framework to use
- The frontend is using Angular 16. For css, Bulma is being used. 
- The backend is using Java 17 with Spring Boot framework.

#### Test coverage
You are expected to provide full unit test coverage for your implementation

## How to run
To run this project, it is recommended to installed Docker.

Use `docker-compose up`. 

This will get both project, backend and frontend, 
build them and allow you to access it as [http://localhost:8888](http://localhost:8888).

## References

- https://www.baeldung.com/spring-boot-evict-cache
- https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.testcontainers.service-connections