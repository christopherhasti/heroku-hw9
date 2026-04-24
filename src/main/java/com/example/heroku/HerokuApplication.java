package com.example.heroku;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Controller
@SpringBootApplication
public class HerokuApplication {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(HerokuApplication.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      
      System.out.println("Print statement inside the Main.db method. Christopher Hastings");

      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (tick timestamp, random_string varchar(50))");
      
      String randomStr = UUID.randomUUID().toString().substring(0, 8);
      stmt.executeUpdate("INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + randomStr + "')");
      
      ResultSet rs = stmt.executeQuery("SELECT * FROM table_timestamp_and_random_string");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick") + " " + rs.getString("random_string"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping(value = "/dbinput", method = RequestMethod.GET)
  @ResponseBody
  public String dbInput() {
      return "<h1>Add Custom String</h1>" +
             "<form method='POST' action='/dbinput'>" +
             "<input type='text' name='userInput' required placeholder='Enter a string...'/>" +
             "<button type='submit'>Submit</button>" +
             "</form>";
  }

  @RequestMapping(value = "/dbinput", method = RequestMethod.POST)
  public String handleDbInput(@RequestParam("userInput") String userInput, Map<String, Object> model) {
      try (Connection connection = dataSource.getConnection()) {
          Statement stmt = connection.createStatement();
          stmt.executeUpdate("INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + userInput + "')");
          return "redirect:/db";
      } catch (Exception e) {
          model.put("message", e.getMessage());
          return "error";
      }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }
}