package by.epam.gym.dao;

import by.epam.gym.entities.Entity;
import by.epam.gym.exceptions.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract root class of DAO level that provide access to the database and deal with application entities.
 *
 * @param <T> the entity type.
 * @author Eugene Makarenko
 * @see Connection
 * @see Entity
 * @see DAOException
 */
public abstract class AbstractDAOImpl<T extends Entity> implements DAO<T> {

    private static final String SELECT_FROM_SQL_QUERY_PART = "SELECT * FROM ";
    private static final String DELETE_SQL_QUERY_PART = "DELETE FROM ";
    private static final String WHERE_SQL_QUERY_PART = " WHERE id=?";

    protected Connection connection;

    public AbstractDAOImpl(Connection connection) {
        this.connection = connection;
    }

    /**
     * This method finds all entities.
     *
     * @return List of found objects.
     * @throws DAOException object if execution of query is failed.
     */
    public List<T> findAll() throws DAOException {
        String sqlQuery = SELECT_FROM_SQL_QUERY_PART + getTableName();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            List<T> entities = new ArrayList<T>();

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T entity = buildEntity(resultSet);
                entities.add(entity);
            }

            return entities;
        } catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method finds entity from database by id.
     *
     * @param id the entity's id.
     * @return the entity.
     * @throws DAOException object if execution of query is failed.
     */
    public T findEntityById(int id) throws DAOException {
        String sqlQuery = SELECT_FROM_SQL_QUERY_PART + getTableName() + WHERE_SQL_QUERY_PART;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, id);

            T entity = null;

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                entity = buildEntity(resultSet);
            }

            return entity;
        } catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method deletes entity from database by id.
     *
     * @param id entity id.
     * @throws DAOException object if execution of query is failed.
     */
    public void deleteById(int id) throws DAOException {
        String sqlQuery = DELETE_SQL_QUERY_PART + getTableName() + WHERE_SQL_QUERY_PART;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, id);

            int queryResult = preparedStatement.executeUpdate();
            if (queryResult != 1){
                throw new DAOException("On delete modify more then 1 record: " + queryResult);
            }

        } catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method creates entity in database.
     *
     * @param entity the entity.
     * @return boolean true if entity created successfully, otherwise false.
     * @throws DAOException object if execution of query is failed.
     */
    public abstract void insert(T entity) throws DAOException;

    public abstract void update(T entity) throws DAOException;

    /**
     * Factory method creates entity.
     *
     * @param resultSet the result set of statement.
     * @return the entity.
     * @throws DAOException object if execution of query is failed.
     */
    public abstract T buildEntity(ResultSet resultSet) throws DAOException;

    /**
     * Gets table name for current DAO.
     *
     * @return the table name.
     */
    public abstract String getTableName();
}