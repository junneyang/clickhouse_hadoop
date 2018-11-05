package data.bytedance.net.ck.hive;

import data.bytedance.net.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClickHouseHelper {
    private static final Logger logger = LoggerFactory.getLogger(ClickHouseHelper.class);
    private static HashMap<Tuple<String, String>, ClickHouseHelper> ckHelperCache = new HashMap<>();
    private final String connStr;
    private final String tableName;
    private List<String> columnNames = new ArrayList<>();
    private List<String> columnTypes = new ArrayList<>();
    private HashMap<String, String> nameTypeMap = new HashMap<>();

    static {
        try {
            Class.forName("com.github.housepower.jdbc.ClickHouseDriver");
        } catch (ClassNotFoundException e) {
            logger.error("Can't find suitable driver", e);
        }
    }

    public static ClickHouseHelper getClickHouseHelper(String connStr, String tableName) throws SQLException {
        Tuple<String, String> k = new Tuple<>(connStr, tableName);
        if (ckHelperCache.containsKey(k)) {
            return ckHelperCache.get(k);
        } else {
            ClickHouseHelper helper = new ClickHouseHelper(connStr, tableName);
            ckHelperCache.put(k, helper);
            return helper;
        }
    }


    private ClickHouseHelper(String connStr, String tableName) throws SQLException {
        this.connStr = connStr;
        this.tableName = tableName;
        initColumnNamesAndTypesFromSystemQuery();
    }

    public Connection getClickHouseConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(getConnStr());
        return connection;
    }


    public void initColumnNamesAndTypesFromSystemQuery() throws SQLException {
        Connection conn = getClickHouseConnection();
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT name, type from system.columns where table = '" + getTableName() + "';";
            logger.info("Initializing columns and types with " + query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                this.columnNames.add(rs.getString(1));
                this.columnTypes.add(rs.getString(2));
                nameTypeMap.put(rs.getString(1), rs.getString(2));
            }
        } finally {
            conn.close();
        }
    }

    public HashMap<String, String> getNameTypeMap() {
        return nameTypeMap;
    }

    public String getConnStr() {
        return connStr;
    }

    public String getTableName() {
        return tableName;
    }


    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> getColumnTypes() {
        return columnTypes;
    }
}
