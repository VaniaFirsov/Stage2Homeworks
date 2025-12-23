package ru.firsov;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    static {
        try {
            System.out.println("Инициализация Hibernate");
            buildSessionFactoryWithConfiguration();
            System.out.println("SessionFactory создана");
        } catch (Throwable ex) {
            System.err.println("\nОшибка инциализации Hibernate");
            System.err.println("Сообщение: " + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void buildSessionFactoryWithConfiguration() {
        Configuration configuration = new Configuration();
        try {
            configuration.configure("hibernate.cfg.xml");
            System.out.println("Конфигурация загружена из hibernate.cfg.xml");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        configuration.addAnnotatedClass(User.class);
        sessionFactory = configuration.buildSessionFactory();
    }
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory не инициализирована");
        }
        return sessionFactory;
    }
    public static void shutdown() {
        if (sessionFactory != null) {
            try {
                sessionFactory.close();
                System.out.println("SessionFactory закрыта");
            } catch (Exception e) {
                System.err.println("Ошибка при закрытии SessionFactory: " + e.getMessage());
            }
        }
    }
}