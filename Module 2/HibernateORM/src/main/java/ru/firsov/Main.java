package ru.firsov;

public class Main {
    public static void main(String[] args) {
        System.out.println("Запуск User Service Application...");
        try {
            ConsoleMenu menu = new ConsoleMenu();
            menu.start();
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
            System.out.println("Приложение завершено.");
        }
    }
    private static void shutdown() {
        try {
            HibernateUtil.shutdown();
            System.out.println("Ресурсы базы данных освобождены.");
        } catch (Exception e) {
            System.err.println("Ошибка при завершении работы: " + e.getMessage());
        }
    }
}