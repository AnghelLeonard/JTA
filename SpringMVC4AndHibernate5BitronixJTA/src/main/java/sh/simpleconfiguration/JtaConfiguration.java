package sh.simpleconfiguration;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.transaction.jta.platform.internal.BitronixJtaPlatform;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import sh.dao.ProductDAO;
import sh.dao.ProductDAOImpl;
import sh.service.ProductService;
import sh.service.ProductServiceImpl;

/**
 *
 * @author Anghel Leonard
 */
@Configuration
@EnableWebMvc
@PropertySource({"/META-INF/jta-mysql.properties"})
@ComponentScan(basePackages = "sh.controllers")
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class JtaConfiguration {

    @Value("${jdbc.dataSourceClassName}")
    private String dataSourceClassName;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.username}")
    private String jdbcUser;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Value("${btm.config.journal:null}")
    private String btmJournal;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;

    //////////////////////////////////////////////////////////////////
    @Bean
    public ProductService productService() {
        return new ProductServiceImpl();
    }

    @Bean
    public ProductDAO productDAO() {
        return new ProductDAOImpl();
    }
    ////////////////////////////////////////////////////////////////////

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

    @Bean
    public DataSource actualDataSource() {

        PoolingDataSource poolingDataSource = new PoolingDataSource();

        poolingDataSource.setClassName(dataSourceClassName);
        poolingDataSource.setUniqueName(getClass().getName());
        poolingDataSource.setMinPoolSize(0);
        poolingDataSource.setMaxPoolSize(5);
        poolingDataSource.setShareTransactionConnections(true);
        poolingDataSource.setAllowLocalTransactions(true);
        poolingDataSource.setAutomaticEnlistingEnabled(true);
        poolingDataSource.setTestQuery("SELECT 1");
        poolingDataSource.setDriverProperties(new Properties());
        poolingDataSource.getDriverProperties().put("user", jdbcUser);
        poolingDataSource.getDriverProperties().put("password", jdbcPassword);
        poolingDataSource.getDriverProperties().put("url", jdbcUrl);

        return new TransactionAwareDataSourceProxy(poolingDataSource);
    }

    @Bean
    @DependsOn(value = {"btmConfig", "transactionManager"})
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setPersistenceUnitName(getClass().getSimpleName());
        localContainerEntityManagerFactoryBean.setPersistenceProvider(new HibernatePersistenceProvider());
        localContainerEntityManagerFactoryBean.setJtaDataSource(actualDataSource());
        localContainerEntityManagerFactoryBean.setPackagesToScan(packagesToScan());

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        localContainerEntityManagerFactoryBean.setJpaProperties(additionalProperties());
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public bitronix.tm.Configuration btmConfig() {
        bitronix.tm.Configuration configuration = TransactionManagerServices.getConfiguration();
        configuration.setServerId("spring-btx-server");
        configuration.setWarnAboutZeroResourceTransaction(true);
        configuration.setJournal(btmJournal);
        return configuration;
    }

    @Bean(destroyMethod = "shutdown")
    @DependsOn(value = "btmConfig")
    public BitronixTransactionManager jtaTransactionManager() {
        return TransactionManagerServices.getTransactionManager();
    }

    @Bean
    @Primary
    public JtaTransactionManager transactionManager() {
        BitronixTransactionManager bitronixTransactionManager = jtaTransactionManager();

        JtaTransactionManager transactionManager = new JtaTransactionManager();
        transactionManager.setTransactionManager(bitronixTransactionManager);
        transactionManager.setUserTransaction(bitronixTransactionManager);
        transactionManager.setAllowCustomIsolationLevels(true);

        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager());
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

    protected String[] packagesToScan() {
        return new String[]{
            "sh.model"
        };
    }

    public static int batchSize() {
        return Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE);
    }
}
