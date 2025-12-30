package testutil;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.firsov.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.firsov.dao.UserDAO;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static SessionFactory sessionFactory;
    private static UserDAO userDAO;

    @BeforeAll
    static void setup() {
        System.setProperty("db.url", postgres.getJdbcUrl());
        System.setProperty("db.username", postgres.getUsername());
        System.setProperty("db.password", postgres.getPassword());

        sessionFactory = TestDatabaseConfig.getTestSessionFactory();
        overrideHibernateUtil();

        userDAO = new UserDAO();
    }

    @AfterAll
    static void tearDown() {
        TestDatabaseConfig.shutdown();
    }

    private static void overrideHibernateUtil() {
        try {
            java.lang.reflect.Field field =
                    ru.firsov.HibernateUtil.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            field.set(null, sessionFactory);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось переопределить HibernateUtil", e);
        }
    }

    @BeforeEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            tx.commit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Сохранение пользователя")
    void save_shouldSaveUserAndGenerateId() {
        User user = TestDataFactory.createValidUser();
        User savedUser = userDAO.save(user);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertTrue(savedUser.getId() > 0);
        assertEquals("Иван Иванов", savedUser.getName());
        assertEquals("ivan@example.com", savedUser.getEmail());
        assertEquals(30, savedUser.getAge());
    }

    @Test
    @Order(2)
    @DisplayName("Поиск пользователя по ID")
    void findById_shouldReturnUserWhenExists() {
        User user = userDAO.save(TestDataFactory.createValidUser());
        Long userId = user.getId();
        Optional<User> foundUser = userDAO.findById(userId);
        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("ivan@example.com", foundUser.get().getEmail());
    }

    @Test
    @Order(3)
    @DisplayName("Поиск по несуществующему ID")
    void findById_shouldReturnEmptyOptionalWhenNotFound() {
        Optional<User> foundUser = userDAO.findById(999L);
        assertFalse(foundUser.isPresent());
    }

    @Test
    @Order(4)
    @DisplayName("Поиск по некорректному ID")
    void findById_shouldReturnEmptyForInvalidId() {
        assertFalse(userDAO.findById(-1L).isPresent());
        assertFalse(userDAO.findById(0L).isPresent());
        assertFalse(userDAO.findById(null).isPresent());
    }

    @Test
    @Order(5)
    @DisplayName("Получение всех пользователей")
    void findAll_shouldReturnAllUsers() {
        userDAO.save(TestDataFactory.createUser("Анна", "anna@test.com", 25));
        userDAO.save(TestDataFactory.createUser("Петр", "petr@test.com", 35));
        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("anna@test.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("petr@test.com")));
    }

    @Test
    @Order(6)
    @DisplayName("Обновление пользователя")
    void update_shouldUpdateUserData() {
        User user = userDAO.save(TestDataFactory.createValidUser());
        user.setName("Иван Петров");
        user.setAge(31);
        User updatedUser = userDAO.update(user);
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals("Иван Петров", updatedUser.getName());
        assertEquals(31, updatedUser.getAge());
    }

    @Test
    @Order(7)
    @DisplayName("Удаление пользователя")
    void delete_shouldRemoveUser() {
        User user = userDAO.save(TestDataFactory.createValidUser());
        Long userId = user.getId();
        assertTrue(userDAO.existsById(userId));
        userDAO.delete(userId);
        assertFalse(userDAO.existsById(userId));
        assertFalse(userDAO.findById(userId).isPresent());
    }
    @Test
    @Order(8)
    @DisplayName("Проверка существования пользователя")
    void existsById_shouldReturnCorrectBoolean() {
        // Arrange
        User user = userDAO.save(TestDataFactory.createValidUser());
        Long userId = user.getId();
        assertTrue(userDAO.existsById(userId));
        assertFalse(userDAO.existsById(999L));
        assertFalse(userDAO.existsById(-1L));
        assertFalse(userDAO.existsById(null));
    }
    @Test
    @Order(9)
    @DisplayName("Поиск пользователя по email")
    void findByEmail_shouldReturnUser() {
        userDAO.save(TestDataFactory.createValidUser());
        Optional<User> foundUser = userDAO.findByEmail("ivan@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("ivan@example.com", foundUser.get().getEmail());
    }
    @Test
    @Order(10)
    @DisplayName("Поиск по email с разным регистром")
    void findByEmail_shouldBeCaseInsensitive() {
        userDAO.save(TestDataFactory.createValidUser());
        Optional<User> foundUser1 = userDAO.findByEmail("IVAN@EXAMPLE.COM");
        Optional<User> foundUser2 = userDAO.findByEmail("Ivan@Example.Com");
        assertTrue(foundUser1.isPresent());
        assertTrue(foundUser2.isPresent());
    }

    @Test
    @Order(11)
    @DisplayName("Исключение при ошибке БД")
    void save_shouldThrowDataAccessExceptionOnError() {
        User user = TestDataFactory.createUser("Test", null, 20);
        assertThrows(UserDAO.DataAccessException.class, () -> {
            userDAO.save(user);
        });
    }

    @Test
    @Order(12)
    @DisplayName("Транзакционность")
    void transaction_shouldRollbackOnException() {
        User validUser = TestDataFactory.createValidUser();
        User invalidUser = new User();
        assertThrows(UserDAO.DataAccessException.class, () -> {
            userDAO.save(validUser);
            userDAO.save(invalidUser);
        });
        List<User> users = userDAO.findAll();
        assertTrue(users.isEmpty(), "Данные должны быть откачены");
    }
}
