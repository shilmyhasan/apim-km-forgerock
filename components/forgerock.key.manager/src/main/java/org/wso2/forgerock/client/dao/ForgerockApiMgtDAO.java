package org.wso2.forgerock.client.dao;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.forgerock.client.ForgerockConstants;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class provides the DAO implementation for "Forgerock" connector
 */
public class ForgerockApiMgtDAO {

    private static ForgerockApiMgtDAO INSTANCE = null;
    private static final Log log = LogFactory.getLog(ForgerockApiMgtDAO.class);

    /**
     * Method to get the instance of the ApiMgtDAO.
     *
     * @return {@link ForgerockApiMgtDAO} instance
     */
    public static ForgerockApiMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgerockApiMgtDAO();
        }

        return INSTANCE;
    }

    /**
     * Method returns the appInfo relevant to a consumer key and key manager type.
     * @param clientId consumer key of the app
     * @param keyManager Key manager type
     * @return String app info as a string
     * @throws APIManagementException This is the custom exception class for API management
     */
    public String getAppInfoFromClientId(String clientId, String keyManager) throws APIManagementException {
        String GET_KEY_MAPPING_INFO_FROM_CLIENT_ID = "SELECT APP_INFO FROM AM_APPLICATION_KEY_MAPPING WHERE " +
                "CONSUMER_KEY = ? and KEY_MANAGER = ?";
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(GET_KEY_MAPPING_INFO_FROM_CLIENT_ID)) {
            preparedStatement.setString(1, clientId);
            preparedStatement.setString(2, keyManager);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    try (InputStream appInfo = resultSet.getBinaryStream(ForgerockConstants.APP_UNFO)) {
                        if (appInfo != null) {
                            return (IOUtils.toString(appInfo));
                        }
                    } catch (IOException e) {
                        log.error("Error while retrieving metadata", e);
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while Retrieving Key Mappings ", e);
        }
        return null;
    }
}