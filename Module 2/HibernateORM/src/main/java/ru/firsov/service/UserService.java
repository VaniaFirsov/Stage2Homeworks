package ru.firsov.service;

import ru.firsov.dao.UserDAO;
import ru.firsov.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MAX_AGE = 150;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public UserService() {
        this.userDAO = new UserDAO();
        logger.info("UserService инициализирован");
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        logger.info("UserService инициализирован с кастомным DAO");
    }

    public User createUser(String name, String email, Integer age) {
        logger.info("Создание нового пользователя: name='{}', email='{}', age={}",
                name, email, age);

        validateName(name);
        validateEmail(email);
        validateAge(age);

        String normalizedName = name.trim();
        String normalizedEmail = email.trim().toLowerCase();

        checkEmailUniqueness(normalizedEmail);

        User user = new User();
        user.setName(normalizedName);
        user.setEmail(normalizedEmail);
        user.setAge(age);

        User savedUser = userDAO.save(user);

        logger.info("Пользователь успешно создан: ID={}, Email={}",
                savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("Запрос пользователя по ID: {}", id);
        if (!isValidId(id)) {
            logger.warn("Некорректный ID пользователя: {}", id);
            return Optional.empty();
        }
        Optional<User> user = userDAO.findById(id);
        if (user.isPresent()) {
            logger.debug("Пользователь найден: ID={}, Name={}",
                    id, user.get().getName());
        } else {
            logger.debug("Пользователь с ID={} не найден", id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        logger.debug("Запрос всех пользователей");
        List<User> users = userDAO.findAll();
        logger.info("Получено {} пользователей", users.size());
        return users;
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Обновление пользователя ID={}: name='{}', email='{}', age={}",
                id, name, email, age);
        if (!isValidId(id)) {
            throw new IllegalArgumentException("Некорректный ID пользователя: " + id);
        }
        User user = userDAO.findById(id)
                .orElseThrow(() -> {
                    String errorMsg = String.format("Пользователь с ID=%d не найден", id);
                    logger.error(errorMsg);
                    return new IllegalArgumentException(errorMsg);
                });
        boolean isModified = false;
        if (name != null && !name.trim().isEmpty()) {
            String newName = name.trim();
            validateName(newName);

            if (!newName.equals(user.getName())) {
                user.setName(newName);
                isModified = true;
                logger.debug("Имя пользователя обновлено");
            }
        }
        if (email != null && !email.trim().isEmpty()) {
            String newEmail = email.trim().toLowerCase();
            validateEmail(newEmail);

            if (!newEmail.equals(user.getEmail())) {
                checkEmailAvailabilityForUpdate(id, newEmail);
                user.setEmail(newEmail);
                isModified = true;
                logger.debug("Email пользователя обновлен");
            }
        }
        if (age != null) {
            validateAge(age);
            if (!age.equals(user.getAge())) {
                user.setAge(age);
                isModified = true;
                logger.debug("Возраст пользователя обновлен");
            }
        }
        if (isModified) {
            User updatedUser = userDAO.update(user);
            logger.info("Пользователь ID={} успешно обновлен", id);
            return updatedUser;
        } else {
            logger.debug("Данные пользователя ID={} не изменились", id);
            return user;
        }
    }

    public boolean deleteUser(Long id) {
        logger.info("Удаление пользователя ID={}", id);
        if (!isValidId(id)) {
            logger.warn("Некорректный ID для удаления: {}", id);
            return false;
        }
        if (!userDAO.existsById(id)) {
            logger.warn("Пользователь с ID={} не существует, удаление невозможно", id);
            return false;
        }
        try {
            userDAO.delete(id);
            logger.info("Пользователь ID={} успешно удален", id);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя ID={}: {}",
                    id, e.getMessage(), e);
            throw new ServiceException("Не удалось удалить пользователя", e);
        }
    }
    public boolean userExists(Long id) {
        boolean exists = userDAO.existsById(id);
        logger.trace("Пользователь ID={} существует: {}", id, exists);
        return exists;
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Имя должно содержать минимум %d символа", MIN_NAME_LENGTH));
        }
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Имя не может превышать %d символов", MAX_NAME_LENGTH));
        }
    }
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        String trimmedEmail = email.trim();

        if (trimmedEmail.length() > MAX_EMAIL_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Email не может превышать %d символов", MAX_EMAIL_LENGTH));
        }

        if (!trimmedEmail.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Некорректный формат email");
        }
    }

    private void validateAge(Integer age) {
        if (age == null) {
            return;
        }
        if (age < 0) {
            throw new IllegalArgumentException("Возраст не может быть отрицательным");
        }
        if (age > MAX_AGE) {
            throw new IllegalArgumentException(
                    String.format("Возраст не может превышать %d лет", MAX_AGE));
        }
    }
    private void validateAgeRange(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < 0) {
            throw new IllegalArgumentException("Возраст не может быть отрицательным");
        }

        if (minAge > maxAge) {
            throw new IllegalArgumentException(
                    "Минимальный возраст не может быть больше максимального");
        }
    }
    private void checkEmailUniqueness(String email) {
        if (userDAO.findByEmail(email).isPresent()) {
            String errorMsg = String.format("Пользователь с email '%s' уже существует", email);
            logger.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private void checkEmailAvailabilityForUpdate(Long userId, String newEmail) {
        Optional<User> existingUser = userDAO.findByEmail(newEmail);

        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            String errorMsg = String.format(
                    "Email '%s' уже используется другим пользователем (ID=%d)",
                    newEmail, existingUser.get().getId());
            logger.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public void clearAllUsers() {
        logger.warn("Очистка всех пользователей из БД!");
        List<User> users = getAllUsers();
        int count = users.size();
        for (User user : users) {
            deleteUser(user.getId());
        }
        logger.warn("Удалено {} пользователей", count);
    }

    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}