package by.epam.gym.dao;

import by.epam.gym.entities.user.User;
import by.epam.gym.entities.user.UserRole;
import by.epam.gym.exceptions.DAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that provide access to the database and deal with User entity.
 *
 * @author Eugene Makarenko
 * @see AbstractDAOImpl
 * @see User
 */
public class UserDAOImpl extends AbstractDAOImpl<User> {

    private static final String LOGIN_COLUMN_LABEL = "login";
    private static final String PASSWORD_COLUMN_LABEL = "password";
    private static final String ROLE_COLUMN_LABEL = "role";
    private static final String FIRST_NAME_COLUMN_LABEL = "first_name";
    private static final String LAST_NAME_COLUMN_LABEL = "last_name";
    private static final String IS_PERSONAL_TRAINER_NEED_COLUMN_LABEL = "is_personal_trainer_need";

    private static final int FIRST_COLUMN_INDEX = 1;

    private static final String SPACE = " ";

    private static final String USERS_RESOURCES_FILE_NAME = "users";

    private int numberOfRecords;

    /**
     * Instantiates a new UserDAOImpl.
     *
     * @param connection the connection to database.
     */
    public UserDAOImpl(Connection connection) {
        super(connection,USERS_RESOURCES_FILE_NAME);
    }

    /**
     * This method finds user in database by it's login and password.
     *
     * @param login    the user's login.
     * @param password the user's password.
     * @return the User object.
     * @throws DAOException object if execution of query is failed.
     */
    public User findUserByLoginAndPassword(String login, String password) throws DAOException {
        String sqlQuery = resourceBundle.getString("query.find_by_login_and_password");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            User user = null;
            if (resultSet.next()) {
                user = buildEntity(resultSet);
            }

            return user;
        } catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method checks user's login for unique.
     *
     * @param login the user's login.
     * @return true if login is unique, else returns false.
     * @throws DAOException object if execution of query is failed.
     */
    public boolean checkLoginForUnique(String login) throws DAOException {
        String sqlQuery = resourceBundle.getString("query.check_login_for_unique");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, login);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method finds user by his name.
     *
     * @param firstName the user's name.
     * @param lastName the user's name.
     * @return List of found users.
     * @throws DAOException object if execution of query is failed.
     */
    public List<User> findClientByName(String firstName, String lastName) throws DAOException {
        String sqlQuery = resourceBundle.getString("query.find_client_by_name");
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1,firstName);
            preparedStatement.setString(2,lastName);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<User> findUsers = new ArrayList<>();
            while (resultSet.next()){
                User user = buildEntity(resultSet);

                findUsers.add(user);
            }

            return findUsers;
        }catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method finds all clients in database.
     *
     * @return List of clients.
     * @throws DAOException object if execution of query is failed.
     */
    public List<User> findAllClientsByPages(int offSet, int numberOfRecords) throws DAOException {
        String sqlQueryFirst = resourceBundle.getString("query.find_all_clients_by_pages");
        String sqlQuerySecond = resourceBundle.getString("query.select_found_rows");
        try(Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sqlQueryFirst + offSet + ", " + numberOfRecords);

            List<User> findUsers = new ArrayList<>();
            while (resultSet.next()){
                User user = buildEntity(resultSet);

                findUsers.add(user);
            }

            resultSet = statement.executeQuery(sqlQuerySecond);
            if (resultSet.next()){
                this.numberOfRecords = resultSet.getInt(FIRST_COLUMN_INDEX);
            }

            return findUsers;
        }catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method finds all clients of current trainer and their training programs id .
     *
     * @param trainerId the trainer id.
     * @return Map with results.
     * @throws DAOException object if execution of query is failed.
     */
    public Map<User, Integer> findPersonalClients(int trainerId) throws DAOException {
        String sqlQueryFirst = resourceBundle.getString("query.select_personal_clients");
        String sqlQuerySecond = resourceBundle.getString("query.select_training_program_id");

        try(PreparedStatement findClientsStatement = connection.prepareStatement(sqlQueryFirst);
            PreparedStatement findTrainingProgramIdStatement = connection.prepareStatement(sqlQuerySecond)){

            Map<User, Integer> clientsAndTrainingProgramId = new HashMap<>();

            findClientsStatement.setInt(1,trainerId);

            ResultSet clients = findClientsStatement.executeQuery();

            while (clients.next()){
                User user = buildEntity(clients);

                int clientId = user.getId();
                findTrainingProgramIdStatement.setInt(1,clientId);
                ResultSet foundTrainingProgramId = findTrainingProgramIdStatement.executeQuery();
                foundTrainingProgramId.next();

                int trainingProgramId = foundTrainingProgramId.getInt(ID_COLUMN_LABEL);

                clientsAndTrainingProgramId.put(user,trainingProgramId);
            }

            return clientsAndTrainingProgramId;
        }catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method finds training program author.
     *
     * @param trainingProgramId the training program id.
     * @return the name of author.
     * @throws DAOException object if execution of query is failed.
     */
    public String findTrainingProgramAuthorName(int trainingProgramId) throws DAOException {
        String sqlQuery = resourceBundle.getString("query.select_training_program_author_name");
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)){
            preparedStatement.setInt(1,trainingProgramId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String firstName = resultSet.getString(FIRST_NAME_COLUMN_LABEL);
                String lastName = resultSet.getString(LAST_NAME_COLUMN_LABEL);

                String result = firstName + SPACE + lastName;

                return result;
            } else {
                throw new DAOException("Trainer didn't find.");
            }
        }catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method finds clients ids and names.
     *
     * @return Map with id and name.
     * @throws DAOException object if execution of query is failed.
     */
    public Map<Integer, String> findClientsIdAndName() throws DAOException {
        String sqlQuery = resourceBundle.getString("query.select_client_id_and_name");
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, String> clientsIdAndName = new HashMap<>();

            while (resultSet.next()){

                int id = resultSet.getInt(ID_COLUMN_LABEL);
                String firstName = resultSet.getString(FIRST_NAME_COLUMN_LABEL);
                String lastName = resultSet.getString(LAST_NAME_COLUMN_LABEL);
                String fullName = firstName + SPACE + lastName;

                clientsIdAndName.put(id,fullName);
            }

            return clientsIdAndName;
        }catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method checks client if he needs personal trainer.
     *
     * @param clientId the client's id.
     * @return true if client needs personal trainer and false otherwise.
     * @throws DAOException object if execution of query is failed.
     */
    public boolean isClientNeedPersonalTrainer(int clientId) throws DAOException {
        String sqlQuery = resourceBundle.getString("query.is_personal_trainer_need");
        try(PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1,clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                int value = resultSet.getInt(IS_PERSONAL_TRAINER_NEED_COLUMN_LABEL);

                if (value == 1){
                    return true;
                } else if (value == 0){
                    return false;
                } else {
                    throw new DAOException(String.format("Unexpected result from query - %s.",sqlQuery));
                }
            } else {
                throw new DAOException(String.format("Unexpected result from query - %s.",sqlQuery));
            }

        }catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * This method builds User object from ResultSet object.
     *
     * @param resultSet the result set of statement.
     * @return The User object.
     * @throws DAOException object if execution of query is failed.
     */
    @Override
    public User buildEntity(ResultSet resultSet) throws DAOException {
        try {
            User user = new User();

            String userRoleValue = resultSet.getString(ROLE_COLUMN_LABEL);

            int id = resultSet.getInt(ID_COLUMN_LABEL);
            String login = resultSet.getString(LOGIN_COLUMN_LABEL);
            String password = resultSet.getString(PASSWORD_COLUMN_LABEL);
            UserRole userRole = UserRole.valueOf(userRoleValue);
            String firstName = resultSet.getString(FIRST_NAME_COLUMN_LABEL);
            String lastName = resultSet.getString(LAST_NAME_COLUMN_LABEL);

            user.setId(id);
            user.setLogin(login);
            user.setPassword(password);
            user.setUserRole(userRole);
            user.setFirstName(firstName);
            user.setLastName(lastName);

            return user;
        } catch (SQLException exception) {
            throw new DAOException("SQL exception detected. " + exception);
        }
    }

    /**
     * Gets number of records.
     *
     * @return the number of records.
     */
    public int getNumberOfRecords() {
        return numberOfRecords;
    }
}
