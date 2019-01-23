package com.dhcc.act.config;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;


class ActConfig {

    //act引擎
    private ProcessEngine processEngine;
    //act引擎配置
    private SpringProcessEngineConfiguration springProcessEngineConfiguration;
    //act引擎生成工厂
    private ProcessEngineFactoryBean processEngineFactoryBean;

    public ActConfig() {
        springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineFactoryBean = new ProcessEngineFactoryBean();
    }

    /**
      * @author lht
      * @doc   初始化act事务
      * @date 2018/6/8
      * @param dataSource
      * @return
    */
    private DataSourceTransactionManager initTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    /**
      * @author lht
      * @doc   获得act引擎实例
      * @date 2018/6/8
      * @param dataSource
      * @return
    */
    public final ProcessEngine setProcessEngine(DataSource dataSource) throws Exception {
        return this.setProcessEngine(dataSource, this.initTransactionManager(dataSource));
    }

    /**
      * @author lht
      * @doc   获得act引擎实例
      * @date 2018/6/8
      * @param dataSource
    * @param dataSourceTransactionManager
      * @return
    */
    public final ProcessEngine setProcessEngine(DataSource dataSource, DataSourceTransactionManager dataSourceTransactionManager) throws Exception {
        this.springProcessEngineConfiguration.setTransactionManager(dataSourceTransactionManager);
        this.springProcessEngineConfiguration.setDataSource(dataSource);
        this.processEngineFactoryBean.setProcessEngineConfiguration(this.springProcessEngineConfiguration);
        this.processEngine = this.processEngineFactoryBean.getObject();
        return this.processEngine;
    }
}