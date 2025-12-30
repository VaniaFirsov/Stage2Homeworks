package testutil;

import ru.firsov.User;

public class TestDataFactory {

    public static User createValidUser() {
        User user = new User();
        user.setName("Иван Иванов");
        user.setEmail("ivan@example.com");
        user.setAge(30);
        return user;
    }

    public static User createUser(String name, String email, Integer age) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }
}
