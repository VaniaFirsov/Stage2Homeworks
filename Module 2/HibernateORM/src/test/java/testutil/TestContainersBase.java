package testutil;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class TestContainersBase {

    protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true); // Для ускорения тестов

        POSTGRES_CONTAINER.start();

        // Устанавливаем системные свойства для Hibernate
        System.setProperty("db.url", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("db.username", POSTGRES_CONTAINER.getUsername());
        System.setProperty("db.password", POSTGRES_CONTAINER.getPassword());
    }
}
