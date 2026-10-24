package com.smartFood.donation.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);

        String jdbcUrl = rawUrl != null ? rawUrl.trim() : "";
        String user = username != null ? username.trim() : "";
        String pass = password != null ? password.trim() : "";

        // If URL was provided as mysql:// or mysql2:// (e.g. from Aiven Service URI or Render DATABASE_URL)
        if (jdbcUrl.startsWith("mysql://") || jdbcUrl.startsWith("mysql2://")) {
            try {
                String fakeHttp = jdbcUrl.replaceFirst("^mysql2?://", "http://");
                URI uri = URI.create(fakeHttp);

                if (uri.getUserInfo() != null && !uri.getUserInfo().isEmpty()) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    if (userInfo.length > 0 && !userInfo[0].isEmpty()) {
                        user = userInfo[0];
                    }
                    if (userInfo.length > 1) {
                        pass = userInfo[1];
                    }
                }

                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 3306;
                String path = uri.getPath() != null && !uri.getPath().isEmpty() ? uri.getPath() : "/defaultdb";
                String query = uri.getQuery();

                if (query != null && !query.isEmpty()) {
                    query = query.replace("ssl-mode=", "sslMode=");
                } else {
                    query = "sslMode=REQUIRED";
                }

                jdbcUrl = "jdbc:mysql://" + host + ":" + port + path + "?" + query;
            } catch (Exception e) {
                jdbcUrl = "jdbc:" + jdbcUrl.replace("ssl-mode=", "sslMode=");
            }
        } else if (!jdbcUrl.isEmpty() && !jdbcUrl.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + jdbcUrl;
        }

        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(user);
        ds.setPassword(pass);

        return ds;
    }
}
