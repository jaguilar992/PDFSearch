package files;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
  private Connection conn = null;
  private boolean connected = false;

  public Database() {
    try {
      String dbName = "pdfsearch.sqlite.db";
      this.conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
      connected = true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean createTableIfNotExists() {
    String CREATE_FILES_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS FILES (\n" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
        "    path TEXT NOT NULL,\n" +
        "    size INTEGER NOT NULL,\n" +
        "    numPages INTEGER NOT NULL,\n" +
        "    content TEXT NOT NULL DEFAULT '',\n" +
        "    rootPath TEXT NOT NULL\n" +
        ");";
    try (
        Statement stmt = this.conn.createStatement()
    ) {
      int c = stmt.executeUpdate(CREATE_FILES_TABLE_QUERY);
      return c == 0;
    } catch (SQLException e) {
      e.printStackTrace();
    } return false;
  }

  public long createFileEntry(String path, long size, int numPages, String rootPath) {
    if (this.connected && !(this.fileExists(path))) {
      final String BASE_QUERY = "INSERT INTO FILES(path, size, numPages, rootPath) VALUES (?,?,?,?)";
      try (
          PreparedStatement pstmt = this.conn.prepareStatement(BASE_QUERY)
      ) {
        pstmt.setString(1, path);
        pstmt.setLong(2, size);
        pstmt.setInt(3, numPages);
        pstmt.setString(4, rootPath);
        int c = pstmt.executeUpdate();
        if (c > 0) {
          ResultSet rs = pstmt.getGeneratedKeys();
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
        return 0L;
      } catch (SQLException e) {
        e.printStackTrace();
        return 0L;
      }
    }
    return 0L;
  }


  public void updateFileContent(String path, String content) {
    final String BASE_QUERY = "UPDATE FILES SET content = ? WHERE path = ?";
    if (this.connected) {
      try (PreparedStatement pstmt = this.conn.prepareStatement(BASE_QUERY)) {
        pstmt.setString(1, content);
        pstmt.setString(2, path);
        pstmt.executeUpdate();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public boolean fileExists(String path) {
    if (this.connected) {
      final String BASE_QUERY = "SELECT count(*) as exist\n" +
          "FROM FILES\n" +
          "WHERE\n" +
          " path = ?";
      try (
          PreparedStatement pstmt = this.conn.prepareStatement(BASE_QUERY);
      ) {
        pstmt.setString(1, path);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return rs.getInt("exist") > 0;
        }
      } catch (SQLException e) {
        e.printStackTrace();
        return false;
      }
    }
    return false;
  }

  public HashMap<String, Long> getFiles(String rootPath){
    this.createTableIfNotExists();
    final HashMap<String, Long> data = new HashMap<>();
    final String BASE_QUERY = "SELECT path, size FROM FILES";
    try(
        PreparedStatement pstmt = this.conn.prepareStatement(BASE_QUERY)
    ) {
      ResultSet rs = pstmt.executeQuery();
      String path;
      long size;
      while(rs.next()){
        path = rs.getString("path");
        size = rs.getLong("size");
        data.put(path, size);
      }
      return data;
    } catch (SQLException e){
      e.printStackTrace();
      return null;
    }
  }

  public void close() {
    try {
      this.conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<File> search(String query, String dir) {
    ArrayList<File> data = new ArrayList<>();
    this.createTableIfNotExists();
    final String BASE_QUERY = "SELECT * FROM FILES WHERE content LIKE ? AND path LIKE '%'|| ? ||'%'";

    try (
        PreparedStatement pstmt = this.conn.prepareStatement(BASE_QUERY)
    ) {
      pstmt.setString(1, query);
      pstmt.setString(2, dir);
      ResultSet rs = pstmt.executeQuery();

      while(rs.next()){
        String path = rs.getString("path");
        data.add(new File(path));
      }
      return data;
    } catch (SQLException e){
      e.printStackTrace();
      return null;
    }

  }

  public int getCount(String dir) {
    final String BASE_QUERY = "SELECT COUNT(*) as count FROM FILES WHERE rootPath = ?";
    try (PreparedStatement pstmt = this.conn.prepareStatement(BASE_QUERY)) {
      pstmt.setString(1,dir);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()){
        return rs.getInt("count");
      }
      return 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return 0;
    }
  }
}
