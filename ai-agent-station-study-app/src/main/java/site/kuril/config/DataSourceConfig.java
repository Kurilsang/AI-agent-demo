package site.kuril.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
public class DataSourceConfig {

    /**
     * MySQL业务数据源配置
     */
    @Bean("mysqlDataSource")
    @Primary
    public DataSource mysqlDataSource(
            @Value("${spring.datasource.mysql.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.mysql.url}") String url,
            @Value("${spring.datasource.mysql.username}") String username,
            @Value("${spring.datasource.mysql.password}") String password,
            @Value("${spring.datasource.mysql.hikari.maximum-pool-size:10}") int maximumPoolSize,
            @Value("${spring.datasource.mysql.hikari.minimum-idle:5}") int minimumIdle,
            @Value("${spring.datasource.mysql.hikari.idle-timeout:30000}") long idleTimeout,
            @Value("${spring.datasource.mysql.hikari.connection-timeout:30000}") long connectionTimeout,
            @Value("${spring.datasource.mysql.hikari.max-lifetime:1800000}") long maxLifetime) {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 连接池参数配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        dataSource.setPoolName("MainHikariPool");

        return dataSource;
    }

    /**
     * MyBatis集成配置
     */
    @Bean("sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource mysqlDataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(mysqlDataSource);

        // 设置MyBatis配置文件位置
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(resolver.getResource("classpath:/mybatis/config/mybatis-config.xml"));

        // 设置Mapper XML文件位置
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/mapper/*.xml"));

        return sqlSessionFactoryBean;
    }

    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactoryBean sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(Objects.requireNonNull(sqlSessionFactory.getObject()));
    }

    /**
     * PgVector向量数据源配置
     */
    @Bean("pgVectorDataSource")
    public DataSource pgVectorDataSource(
            @Value("${spring.datasource.pgvector.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.pgvector.url}") String url,
            @Value("${spring.datasource.pgvector.username}") String username,
            @Value("${spring.datasource.pgvector.password}") String password,
            @Value("${spring.datasource.pgvector.hikari.maximum-pool-size:5}") int maximumPoolSize,
            @Value("${spring.datasource.pgvector.hikari.minimum-idle:2}") int minimumIdle,
            @Value("${spring.datasource.pgvector.hikari.idle-timeout:30000}") long idleTimeout,
            @Value("${spring.datasource.pgvector.hikari.connection-timeout:30000}") long connectionTimeout) {

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 向量库专用连接池配置
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setConnectionTimeout(connectionTimeout);

        // 向量库特殊配置
        dataSource.setInitializationFailTimeout(1);
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setAutoCommit(true);
        dataSource.setPoolName("PgVectorHikariPool");

        return dataSource;
    }

    @Bean("pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
} 