package ru.firsov.service;

import ru.firsov.User;
import ru.firsov.dto.UserRequestDTO;
import ru.firsov.dto.UserResponseDTO;
import ru.firsov.exception.UserNotFoundException;
import ru.firsov.model.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        log.info("Создание пользователя: email={}", userRequestDTO.getEmail());

        // Проверка уникальности email
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Пользователь с email " +
                    userRequestDTO.getEmail() + " уже существует");
        }

        // Создание пользователя
        User user = new User();
        user.setName(userRequestDTO.getName().trim());
        user.setEmail(userRequestDTO.getEmail().trim().toLowerCase());
        user.setAge(userRequestDTO.getAge());

        User savedUser = userRepository.save(user);
        log.info("Пользователь создан: id={}, email={}",
                savedUser.getId(), savedUser.getEmail());

        return mapToResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.debug("Получение пользователя по id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));

        return mapToResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Получение всех пользователей");

        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        log.info("Обновление пользователя: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));

        // Обновление данных
        if (userRequestDTO.getName() != null) {
            user.setName(userRequestDTO.getName().trim());
        }

        if (userRequestDTO.getEmail() != null) {
            String newEmail = userRequestDTO.getEmail().trim().toLowerCase();

            // Проверка, что email не занят другим пользователем
            if (!newEmail.equals(user.getEmail())) {
                userRepository.findByEmail(newEmail)
                        .ifPresent(existingUser -> {
                            if (!existingUser.getId().equals(id)) {
                                throw new IllegalArgumentException(
                                        "Email " + newEmail + " уже используется другим пользователем");
                            }
                        });
                user.setEmail(newEmail);
            }
        }

        if (userRequestDTO.getAge() != null) {
            user.setAge(userRequestDTO.getAge());
        }

        User updatedUser = userRepository.save(user);
        log.info("Пользователь обновлен: id={}", id);

        return mapToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя: id={}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Пользователь с id " + id + " не найден");
        }

        userRepository.deleteById(id);
        log.info("Пользователь удален: id={}", id);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAge(user.getAge());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
