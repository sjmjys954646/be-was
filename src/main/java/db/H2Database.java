package db;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class H2Database {private static final Logger logger = LoggerFactory.getLogger(H2Database.class);
    private static final String url = "jdbc:h2:tcp://localhost:9092/~/was";
    private static final String username = "sa";
    private static final String password = "";

    private static Connection conn = null;
    private static PreparedStatement preparedStatemnt = null;

    H2Database() {
        createUserTable();
    }

    public static void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS User (" +
                "userId VARCHAR(255) PRIMARY KEY," +
                "password VARCHAR(255) NOT NULL," +
                "name VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) NOT NULL" +
                ");";

        try (Connection conn = DriverManager.getConnection(url, username, password); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }


    public static void userAdd(User user) {
        try {
            conn = DriverManager.getConnection(url, username, password);

            // 쿼리 실행
            int affectedRows = preparedStatemnt.executeUpdate();
            logger.debug(affectedRows + "실행 완료");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (preparedStatemnt != null)
                    preparedStatemnt.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
