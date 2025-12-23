package ru.firsov.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.firsov.User;
import ru.firsov.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class UserDAO implements DAO<User, Long> {

    private static final Logger logger = LogManager.getLogger(UserDAO.class);
    @Override
    public User save(User user) {
        Transaction transaction = null;
        logger.debug("Начало сохранения пользователя: {}",
                () -> user != null ? user.getEmail() : "null");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("Пользователь успешно сохранен: ID={}, Email={}",
                    user.getId(), user.getEmail());
            return user;
        } catch (Exception e) {
            // откатываем транзакцию при ошибке
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.warn("Транзакция откачена из-за ошибки");
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.error("Ошибка сохранения пользователя: {}",
                    e.getMessage(), e);
            throw new DataAccessException("Не удалось сохранить пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        if (id == null || id <= 0) {
            logger.warn("Передан некорректный ID: {}", id);
            return Optional.empty();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.find(User.class, id);
            if (user != null) {
                logger.debug("Пользователь найден: ID={}, Name={}", id, user.getName());
            } else {
                logger.debug("Пользователь с ID={} не найден", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по ID={}: {}",
                    id, e.getMessage(), e);
            throw new DataAccessException("Не удалось найти пользователя по ID", e);
        }
    }

    @Override
    public List<User> findAll() {
        logger.debug("Получение всех пользователей");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<User> root = cq.from(User.class);
            cq.select(root);
            cq.orderBy(cb.asc(root.get("id")));
            Query<User> query = session.createQuery(cq);
            List<User> users = query.getResultList();
            logger.info("Получено {} пользователей из базы данных", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Ошибка получения списка пользователей: {}",
                    e.getMessage(), e);
            throw new DataAccessException("Не удалось получить список пользователей", e);
        }
    }
    @Override
    public User update(User user) {
        Transaction transaction = null;
        logger.debug("Начало обновления пользователя: ID={}",
                () -> user != null ? user.getId() : "null");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User updatedUser = session.merge(user);
            transaction.commit();
            logger.info("Пользователь успешно обновлен: ID={}, Email={}",
                    updatedUser.getId(), updatedUser.getEmail());
            return updatedUser;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.warn("Транзакция откачена при обновлении");
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.error("Ошибка обновления пользователя ID={}: {}",
                    user != null ? user.getId() : "null",
                    e.getMessage(), e);
            throw new DataAccessException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        logger.debug("Начало удаления пользователя: ID={}", id);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.find(User.class, id);

            if (user != null) {
                session.remove(user);
                transaction.commit();
                logger.info("Пользователь успешно удален: ID={}, Email={}",
                        id, user.getEmail());
            } else {
                transaction.commit();
                logger.warn("Попытка удаления несуществующего пользователя: ID={}", id);
            }

        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                    logger.warn("Транзакция откачена при удалении");
                } catch (Exception rollbackEx) {
                    logger.error("Ошибка при откате транзакции", rollbackEx);
                }
            }
            logger.error("Ошибка удаления пользователя ID={}: {}",
                    id, e.getMessage(), e);
            throw new DataAccessException("Не удалось удалить пользователя", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        logger.trace("Проверка существования пользователя: ID={}", id);
        if (id == null || id <= 0) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.find(User.class, id);
            boolean exists = user != null;
            logger.trace("Пользователь ID={} существует: {}", id, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Ошибка проверки существования пользователя ID={}: {}",
                    id, e.getMessage(), e);
            throw new DataAccessException("Не удалось проверить существование пользователя", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Поиск пользователя по email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            logger.warn("Передан пустой email для поиска");
            return Optional.empty();
        }
        String trimmedEmail = email.trim().toLowerCase();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User u WHERE u.email = :email";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("email", trimmedEmail);
            User user = query.uniqueResult();
            if (user != null) {
                logger.debug("Пользователь найден по email: {} -> ID={}",
                        trimmedEmail, user.getId());
            } else {
                logger.debug("Пользователь с email={} не найден", trimmedEmail);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по email={}: {}",
                    trimmedEmail, e.getMessage(), e);
            throw new DataAccessException("Не удалось найти пользователя по email", e);
        }
    }

    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message) {
            super(message);
        }
        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}