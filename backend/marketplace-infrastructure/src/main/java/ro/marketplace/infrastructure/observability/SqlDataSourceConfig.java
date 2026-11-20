package ro.marketplace.infrastructure.observability;

import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.sql.DataSource;

@Configuration
@EnableAspectJAutoProxy
public class SqlDataSourceConfig {

    @Bean
    public static BeanPostProcessor sqlAnalysisDataSourcePostProcessor(
            SqlAnalysisContext context,
            @Value("${app.sql-analysis.enabled:false}") boolean enabled) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (!enabled || !(bean instanceof DataSource dataSource)) {
                    return bean;
                }
                if (beanName.contains("Sql") || beanName.contains("Proxy")) {
                    return bean;
                }

                QueryExecutionListener listener = new QueryExecutionListener() {
                    @Override
                    public void beforeQuery(net.ttddyy.dsproxy.ExecutionInfo executionInfo,
                                            java.util.List<net.ttddyy.dsproxy.QueryInfo> queryInfoList) {
                    }

                    @Override
                    public void afterQuery(net.ttddyy.dsproxy.ExecutionInfo executionInfo,
                                           java.util.List<net.ttddyy.dsproxy.QueryInfo> queryInfoList) {
                        long elapsed = executionInfo.getElapsedTime();
                        for (net.ttddyy.dsproxy.QueryInfo queryInfo : queryInfoList) {
                            context.record(queryInfo.getQuery(), elapsed);
                        }
                    }
                };

                return ProxyDataSourceBuilder.create(dataSource)
                        .name("sql-analysis")
                        .listener(listener)
                        .build();
            }
        };
    }
}
