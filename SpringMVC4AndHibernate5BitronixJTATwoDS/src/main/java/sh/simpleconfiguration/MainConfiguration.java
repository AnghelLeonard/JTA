package sh.simpleconfiguration;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.engine.transaction.jta.platform.internal.BitronixJtaPlatform;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import sh.dao.DetailDAO;
import sh.dao.DetailDAOImpl;
import sh.dao.ProductDAO;
import sh.dao.ProductDAOImpl;
import sh.service.DbServiceImpl;
import sh.service.DbService;

/**
 *
 * @author Anghel Leonard
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "sh.controllers")
@PropertySource({"/META-INF/jta-detail-mysql.properties"})
@PropertySource({"/META-INF/jta-product-mysql.properties"})
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class MainConfiguration {

    @Value("${jdbc.dataSourceClassName}")
    private String dataSourceClassName;

     @Value("${jdbc.url_d}")
    private String jdbcUrl_d;
    
    @Value("${jdbc.url_p}")
    private String jdbcUrl_p;

    @Value("${jdbc.username}")
    private String jdbcUser;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;
    
    //////////////////////////////////////////////////////////////////   
    @Bean
    public ProductDAO productDAO() {
        return new ProductDAOImpl();
    }

    @Bean
    public DetailDAO detailDAO() {
        return new DetailDAOImpl();
    }

    @Bean
    public DbService productService() {
        return new DbServiceImpl();
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        return viewResolver;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    ////////////////////////////////////////////////////////////////////   

    @Bean
    public DataSource dataSourceProduct() {

        PoolingDataSource poolingDataSource = new PoolingDataSource();

        poolingDataSource.setClassName(dataSourceClassName);
        poolingDataSource.setUniqueName(getClass().getName() + "_product");
        poolingDataSource.setMinPoolSize(0);
        poolingDataSource.setMaxPoolSize(5);
        poolingDataSource.setShareTransactionConnections(true);
        poolingDataSource.setAllowLocalTransactions(true);
        poolingDataSource.setAutomaticEnlistingEnabled(true);
        poolingDataSource.setTestQuery("SELECT 1");
        poolingDataSource.setDriverProperties(new Properties());
        poolingDataSource.getDriverProperties().put("user", jdbcUser);
        poolingDataSource.getDriverProperties().put("password", jdbcPassword);
        poolingDataSource.getDriverProperties().put("url", jdbcUrl_p);

        return new TransactionAwareDataSourceProxy(poolingDataSource);
    }

    @Bean
    public DataSource dataSourceDetail() {

        PoolingDataSource poolingDataSource = new PoolingDataSource();

        poolingDataSource.setClassName(dataSourceClassName);
        poolingDataSource.setUniqueName(getClass().getName() + "_detail");
        poolingDataSource.setMinPoolSize(0);
        poolingDataSource.setMaxPoolSize(5);
        poolingDataSource.setShareTransactionConnections(true);
        poolingDataSource.setAllowLocalTransactions(true);
        poolingDataSource.setAutomaticEnlistingEnabled(true);
        poolingDataSource.setTestQuery("SELECT 1");
        poolingDataSource.setDriverProperties(new Properties());
        poolingDataSource.getDriverProperties().put("user", jdbcUser);
        poolingDataSource.getDriverProperties().put("password", jdbcPassword);
        poolingDataSource.getDriverProperties().put("url", jdbcUrl_d);

        return new TransactionAwareDataSourceProxy(poolingDataSource);
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(true);
        // hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public bitronix.tm.Configuration btmConfig() {
        bitronix.tm.Configuration configuration = TransactionManagerServices.getConfiguration();
        configuration.setServerId(getClass().getName() + "spring-btx-server");
        configuration.setWarnAboutZeroResourceTransaction(true);
        configuration.setJournal(null);
        return configuration;
    }

    @Bean(destroyMethod = "shutdown")
    @DependsOn(value = "btmConfig")
    public BitronixTransactionManager jtaTransactionManager() {
        return TransactionManagerServices.getTransactionManager();
    }

    @Bean
    @DependsOn(value = "jtaTransactionManager")
    public JtaTransactionManager transactionManager() {
        BitronixTransactionManager bitronixTransactionManager = jtaTransactionManager();

        JtaTransactionManager transactionManager = new JtaTransactionManager();
        transactionManager.setTransactionManager(bitronixTransactionManager);
        transactionManager.setUserTransaction(bitronixTransactionManager);
        transactionManager.setAllowCustomIsolationLevels(true);

        return transactionManager;
    }

    @Bean(name = "productEntityManager")
    @DependsOn("transactionManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryp() {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBeanp = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBeanp.setPersistenceUnitName("productPersistenceUnit");
        localContainerEntityManagerFactoryBeanp.setPersistenceProvider(new HibernatePersistenceProvider());
        localContainerEntityManagerFactoryBeanp.setJtaDataSource(dataSourceProduct());
        localContainerEntityManagerFactoryBeanp.setPackagesToScan(packagesToScanProduct());

        localContainerEntityManagerFactoryBeanp.setJpaVendorAdapter(jpaVendorAdapter());
        localContainerEntityManagerFactoryBeanp.setJpaProperties(additionalProperties());
        return localContainerEntityManagerFactoryBeanp;
    }

    @Bean(name = "detailEntityManager")
    @DependsOn("transactionManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("detailPersistenceUnit");
        localContainerEntityManagerFactoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
        localContainerEntityManagerFactoryBean.setJtaDataSource(dataSourceDetail());
        localContainerEntityManagerFactoryBean.setPackagesToScan(packagesToScanDetail());

        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setJpaProperties(additionalProperties());
        return localContainerEntityManagerFactoryBean;
    }

    protected String[] packagesToScanProduct() {
        return new String[]{
            "sh.model.product"
        };
    }

    protected String[] packagesToScanDetail() {
        return new String[]{
            "sh.model.detail"
        };
    }

    protected Properties additionalProperties() {
        Properties properties = new Properties();

        properties.setProperty("javax.persistence.transactionType", "JTA");
        properties.setProperty("hibernate.connection.release_mode", "after_statement");
        properties.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JTATransactionFactory");
        properties.setProperty("hibernate.transaction.manager_lookup_class", "org.hibernate.transaction.BTMTransactionManagerLookup");
        properties.setProperty("hibernate.current_session_context_class", "jta");
        properties.setProperty("hibernate.transaction.jta.platform", BitronixJtaPlatform.class.getName());
        properties.setProperty("hibernate.dialect", hibernateDialect);
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.show_sql", "hibernate.show_sql");
        properties.setProperty("hibernate.format_sql", "true");

        return properties;
    }

}
