package ru.firsov;

import ru.firsov.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleMenu {

    private final UserService userService;
    private final Scanner scanner;
    private boolean running;

    public ConsoleMenu() {
        this.userService = new UserService();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start() {
        printWelcome();

        while (running) {
            printMenu();
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine().trim();
            try {
                processChoice(choice);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
            if (running) {
                pause();
            }
        }

        System.out.println("Программа завершена.");
        scanner.close();
    }

    private void processChoice(String choice) {
        switch (choice) {
            case "1" -> createUser();
            case "2" -> getUserById();
            case "3" -> getAllUsers();
            case "4" -> updateUser();
            case "5" -> deleteUser();
            case "0" -> exit();
            default -> System.out.println("Неверный выбор. Попробуйте снова.");
        }
    }

    private void createUser() {
        System.out.println("\n=== СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ ===");
        System.out.print("Введите имя: ");
        String name = scanner.nextLine().trim();
        System.out.print("Введите email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Введите возраст (или оставьте пустым): ");
        String ageInput = scanner.nextLine().trim();
        try {
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);
            User user = userService.createUser(name, email, age);
            System.out.println("Пользователь создан успешно!");
            printUser(user);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат возраста!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
    private void getUserById() {
        System.out.println("\n=== ПОИСК ПОЛЬЗОВАТЕЛЯ ПО ID ===");
        System.out.print("Введите ID пользователя: ");
        try {
            Long id = Long.parseLong(scanner.nextLine().trim());
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                System.out.println("Пользователь найден:");
                printUser(user.get());
            } else {
                System.out.println("Пользователь с ID " + id + " не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID!");
        }
    }

    private void getAllUsers() {
        System.out.println("\n=== ВСЕ ПОЛЬЗОВАТЕЛИ ===");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("База данных пуста.");
            return;
        }
        System.out.println("Всего пользователей: " + users.size());
        System.out.println("+-----+-----------------------+---------------------------+------+-------------------+");
        System.out.println("| ID  | Имя                   | Email                     | Возр | Дата создания     |");
        System.out.println("+-----+-----------------------+---------------------------+------+-------------------+");

        for (User user : users) {
            String ageStr = user.getAge() != null ? String.valueOf(user.getAge()) : "N/A";
            String createdAt = user.getCreatedAt() != null ?
                    user.getCreatedAt().toString().substring(0, 16).replace("T", " ") : "N/A";
            System.out.printf("| %-3d | %-21s | %-25s | %-4s | %-17s |\n",
                    user.getId(),
                    truncate(user.getName(), 21),
                    truncate(user.getEmail(), 25),
                    ageStr,
                    truncate(createdAt, 17));
        }

        System.out.println("+-----+-----------------------+---------------------------+------+-------------------+");
    }

    private void updateUser() {
        System.out.println("\n=== ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===");
        System.out.print("Введите ID пользователя для обновления: ");

        try {
            Long id = Long.parseLong(scanner.nextLine().trim());

            if (!userService.userExists(id)) {
                System.out.println("Пользователь с ID " + id + " не найден.");
                return;
            }

            System.out.println("Оставьте поле пустым, если не хотите его изменять.");

            System.out.print("Новое имя: ");
            String name = scanner.nextLine().trim();

            System.out.print("Новый email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Новый возраст: ");
            String ageInput = scanner.nextLine().trim();

            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            User updatedUser = userService.updateUser(id,
                    name.isEmpty() ? null : name,
                    email.isEmpty() ? null : email,
                    age);
            System.out.println("Пользователь обновлен успешно!");
            printUser(updatedUser);
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат данных!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void deleteUser() {
        System.out.println("\n=== УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===");
        System.out.print("Введите ID пользователя для удаления: ");
        try {
            Long id = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Вы уверены? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                boolean deleted = userService.deleteUser(id);
                if (deleted) {
                    System.out.println("Пользователь удален успешно!");
                } else {
                    System.out.println("Пользователь не найден.");
                }
            } else {
                System.out.println("Удаление отменено.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Неверный формат ID!");
        }
    }

    private void exit() {
        System.out.println("\nЗавершение работы...");
        running = false;
    }
    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("    СИСТЕМА УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ   ");
        System.out.println("    (Hibernate + PostgreSQL)           ");
        System.out.println("========================================\n");
    }

    private void printMenu() {
        System.out.println("\n===== ГЛАВНОЕ МЕНЮ =====");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти по ID");
        System.out.println("3. Показать всех");
        System.out.println("4. Обновить данные");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выйти");
    }

    private void pause() {
        System.out.print("\nНажмите Enter для продолжения...");
        scanner.nextLine();
    }

    private void printUser(User user) {
        System.out.println("\n-----------------------------");
        System.out.println("ID:           " + user.getId());
        System.out.println("Имя:          " + user.getName());
        System.out.println("Email:        " + user.getEmail());
        System.out.println("Возраст:      " + (user.getAge() != null ? user.getAge() + " лет" : "не указан"));
        System.out.println("Дата создания: " + user.getCreatedAt());
        System.out.println("-----------------------------");
    }

    private String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }
}
