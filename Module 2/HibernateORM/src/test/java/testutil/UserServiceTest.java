package testutil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.firsov.User;
import ru.firsov.dao.UserDAO;
import ru.firsov.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Unit тесты для UserService")
class UserServiceTest {
    @Mock
    private UserDAO userDAO;
    @InjectMocks
    private UserService userService;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    @BeforeEach
    void setUp() {

    }

    @Test
    @Order(1)
    @DisplayName("Создание валидного пользователя")
    void createUser_shouldCreateUserWhenDataIsValid() {
        String name = "Алексей Петров";
        String email = "alex@example.com";
        Integer age = 25;
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setName(name);
        expectedUser.setEmail(email);
        expectedUser.setAge(age);
        when(userDAO.findByEmail(email.toLowerCase())).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenReturn(expectedUser);
        User createdUser = userService.createUser(name, email, age);

        assertNotNull(createdUser);
        assertEquals(1L, createdUser.getId());
        assertEquals(name, createdUser.getName());
        assertEquals(email.toLowerCase(), createdUser.getEmail());
        assertEquals(age, createdUser.getAge());

        verify(userDAO).findByEmail(email.toLowerCase());
        verify(userDAO).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(name, capturedUser.getName());
        assertEquals(email.toLowerCase(), capturedUser.getEmail());
        assertEquals(age, capturedUser.getAge());
    }

    @Test
    @Order(2)
    @DisplayName("Создание пользователя с существующим email")
    void createUser_shouldThrowExceptionWhenEmailExists() {
        String name = "Алексей Петров";
        String email = "alex@example.com";
        Integer age = 25;

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(email);

        when(userDAO.findByEmail(email.toLowerCase())).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, email, age));

        assertTrue(exception.getMessage().contains("уже существует"));
        verify(userDAO).findByEmail(email.toLowerCase());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    @Order(3)
    @DisplayName("Создание пользователя с невалидным именем")
    void createUser_shouldThrowExceptionForInvalidName() {
        String invalidName = "A";
        String email = "test@example.com";
        Integer age = 25;

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(invalidName, email, age));

        verify(userDAO, never()).findByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    @Order(4)
    @DisplayName("Создание пользователя с невалидным email")
    void createUser_shouldThrowExceptionForInvalidEmail() {
        String name = "Иван Иванов";
        String invalidEmail = "invalid-email";
        Integer age = 25;

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, invalidEmail, age));

        verify(userDAO, never()).findByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    @Order(5)
    @DisplayName("Создание пользователя с невалидным возрастом")
    void createUser_shouldThrowExceptionForInvalidAge() {
        String name = "Иван Иванов";
        String email = "ivan@example.com";
        Integer invalidAge = -5;

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(name, email, invalidAge));

        verify(userDAO, never()).findByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    @Order(6)
    @DisplayName("Получение пользователя по ID")
    void getUserById_shouldReturnUserWhenExists() {
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setName("Иван Иванов");

        when(userDAO.findById(userId)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userDAO).findById(userId);
    }

    @Test
    @Order(7)
    @DisplayName("Получение несуществующего пользователя")
    void getUserById_shouldReturnEmptyForNonExistingUser() {
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userDAO).findById(userId);
    }

    @Test
    @Order(8)
    @DisplayName("Получение пользователя с некорректным ID")
    void getUserById_shouldReturnEmptyForInvalidId() {
        Optional<User> result1 = userService.getUserById(-1L);
        Optional<User> result2 = userService.getUserById(0L);
        Optional<User> result3 = userService.getUserById(null);

        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
        assertFalse(result3.isPresent());

        verify(userDAO, never()).findById(anyLong());
    }

    @Test
    @Order(9)
    @DisplayName("Получение всех пользователей")
    void getAllUsers_shouldReturnUserList() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        List<User> expectedUsers = List.of(user1, user2);
        when(userDAO.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDAO).findAll();
    }

    @Test
    @Order(10)
    @DisplayName("Обновление пользователя")
    void updateUser_shouldUpdateUserWhenDataIsValid() {
        Long userId = 1L;
        String newName = "Новое Имя";
        String newEmail = "new@example.com";
        Integer newAge = 30;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Старое Имя");
        existingUser.setEmail("old@example.com");
        existingUser.setAge(25);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(newName);
        updatedUser.setEmail(newEmail);
        updatedUser.setAge(newAge);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.findByEmail(newEmail.toLowerCase())).thenReturn(Optional.empty());
        when(userDAO.update(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, newName, newEmail, newAge);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(newName, result.getName());
        assertEquals(newEmail.toLowerCase(), result.getEmail());
        assertEquals(newAge, result.getAge());

        verify(userDAO).findById(userId);
        verify(userDAO).findByEmail(newEmail.toLowerCase());
        verify(userDAO).update(any(User.class));
    }

    @Test
    @Order(11)
    @DisplayName("Обновление только имени пользователя")
    void updateUser_shouldUpdateOnlyName() {
        Long userId = 1L;
        String newName = "Новое Имя";

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Старое Имя");
        existingUser.setEmail("test@example.com");
        existingUser.setAge(25);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.update(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(userId, newName, null, null);

        assertEquals(newName, result.getName());
        verify(userDAO, never()).findByEmail(anyString());
    }

    @Test
    @Order(12)
    @DisplayName("Обновление с email другого пользователя")
    void updateUser_shouldThrowExceptionWhenEmailBelongsToAnotherUser() {
        Long userId = 1L;
        String newEmail = "existing@example.com";

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail(newEmail);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.findByEmail(newEmail.toLowerCase())).thenReturn(Optional.of(anotherUser));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userId, null, newEmail, null));

        verify(userDAO).findById(userId);
        verify(userDAO).findByEmail(newEmail.toLowerCase());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    @Order(13)
    @DisplayName("Удаление существующего пользователя")
    void deleteUser_shouldReturnTrueWhenUserDeleted() {
        Long userId = 1L;

        when(userDAO.existsById(userId)).thenReturn(true);
        doNothing().when(userDAO).delete(userId);

        boolean result = userService.deleteUser(userId);

        assertTrue(result);
        verify(userDAO).existsById(userId);
        verify(userDAO).delete(userId);
    }

    @Test
    @Order(14)
    @DisplayName("Удаление несуществующего пользователя")
    void deleteUser_shouldReturnFalseWhenUserNotExists() {
        Long userId = 999L;

        when(userDAO.existsById(userId)).thenReturn(false);

        boolean result = userService.deleteUser(userId);

        assertFalse(result);
        verify(userDAO).existsById(userId);
        verify(userDAO, never()).delete(anyLong());
    }

    @Test
    @Order(15)
    @DisplayName("Удаление с некорректным ID")
    void deleteUser_shouldReturnFalseForInvalidId() {
        assertFalse(userService.deleteUser(-1L));
        assertFalse(userService.deleteUser(0L));
        assertFalse(userService.deleteUser(null));

        verify(userDAO, never()).existsById(anyLong());
        verify(userDAO, never()).delete(anyLong());
    }

    @Test
    @Order(16)
    @DisplayName("Проверка существования пользователя")
    void userExists_shouldDelegateToDAO() {
        Long userId = 1L;
        when(userDAO.existsById(userId)).thenReturn(true);

        boolean result = userService.userExists(userId);

        assertTrue(result);
        verify(userDAO).existsById(userId);
    }

    @Test
    @Order(17)
    @DisplayName("Очистка всех пользователей")
    void clearAllUsers_shouldDeleteAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        when(userDAO.findAll()).thenReturn(List.of(user1, user2));
        when(userDAO.existsById(1L)).thenReturn(true);
        when(userDAO.existsById(2L)).thenReturn(true);

        userService.clearAllUsers();

        verify(userDAO).findAll();
        verify(userDAO, times(2)).delete(anyLong());
    }

    @Test
    @Order(18)
    @DisplayName("Исключение ServiceException при ошибке удаления")
    void deleteUser_shouldThrowServiceExceptionOnDAOError() {
        Long userId = 1L;

        when(userDAO.existsById(userId)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(userDAO).delete(userId);

        assertThrows(UserService.ServiceException.class,
                () -> userService.deleteUser(userId));

        verify(userDAO).existsById(userId);
        verify(userDAO).delete(userId);
    }
}
