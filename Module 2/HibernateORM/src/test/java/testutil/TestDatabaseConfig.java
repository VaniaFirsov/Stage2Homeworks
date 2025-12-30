package testutil;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.firsov.User;

import java.util.Properties;

public class TestDatabaseConfig {

    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory getTestSessionFactory() {
        if (sessionFactory == null) {
            try {
                String jdbcUrl = System.getProperty("db.url",
                        "jdbc:postgresql://localhost:5432/testdb");
                String username = System.getProperty("db.username", "test");
                String password = System.getProperty("db.password", "test");

                Properties hibernateProperties = new Properties();
                hibernateProperties.put("hibernate.connection.driver_class", "org.postgresql.Driver");
                hibernateProperties.put("hibernate.connection.url", jdbcUrl);
                hibernateProperties.put("hibernate.connection.username", username);
                hibernateProperties.put("hibernate.connection.password", password);
                hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                hibernateProperties.put("hibernate.hbm2ddl.auto", "create-drop");
                hibernateProperties.put("hibernate.show_sql", "true");
                hibernateProperties.put("hibernate.format_sql", "true");
                hibernateProperties.put("hibernate.use_sql_comments", "true");
                hibernateProperties.put("hibernate.jdbc.batch_size", "20");
                hibernateProperties.put("hibernate.connection.autocommit", "false");

                Configuration configuration = new Configuration();
                configuration.setProperties(hibernateProperties);
                configuration.addAnnotatedClass(User.class);

                sessionFactory = configuration.buildSessionFactory();
            } catch (Exception e) {
                System.err.println("Ошибка создания SessionFactory: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
