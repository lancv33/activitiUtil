package com.dhcc.act.config;

import com.dhcc.act.utils.ActUtils;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
  * @author lht
  * @doc  生成activiti的service等,activiti的常用方法
 *          也是配置activiti的地方
  * @date 2018/6/6
*/
public class ActEngine implements ProcessEngine{

    private ActUtils actUtils;

    //act引擎
    private ProcessEngine processEngine;
    //act配置
    private ActConfig actConfig;

    public ActUtils getActUtils() {
        return actUtils;
    }

    /**
      * @author lht
      * @doc   创建ActSpringFactory
      * @date 2018/6/8
      * @param dataSource
      * @return
    */
    public ActEngine(DataSource dataSource) {
        this.actConfig = new ActConfig();
        try{
            processEngine = actConfig.setProcessEngine(dataSource);
            actUtils = new ActUtils(processEngine);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
      * @author lht
      * @doc   创建ActSpringFactory
      * @date 2018/6/8
      * @param dataSource
    * @param dataSourceTransactionManager
      * @return
    */
    public ActEngine(DataSource dataSource, DataSourceTransactionManager dataSourceTransactionManager) {
        this.actConfig = new ActConfig();
        try{
            processEngine = actConfig.setProcessEngine(dataSource,dataSourceTransactionManager);
            actUtils = new ActUtils(processEngine);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    
    public RuntimeService getRuntimeService(){
        return processEngine.getRuntimeService();
    }

    public FormService getFormService(){
        return processEngine.getFormService();
    }
    public HistoryService getHistoryService(){
        return processEngine.getHistoryService();
    }
    public IdentityService getIdentityService(){
        return processEngine.getIdentityService();
    }
    public ManagementService getManagementService(){
        return processEngine.getManagementService();
    }

    public DynamicBpmnService getDynamicBpmnService() {
        return processEngine.getDynamicBpmnService();
    }

    public ProcessEngineConfiguration getProcessEngineConfiguration() {
        return processEngine.getProcessEngineConfiguration();
    }

    public FormRepositoryService getFormEngineRepositoryService() {
        return processEngine.getFormEngineRepositoryService();
    }

    public org.activiti.form.api.FormService getFormEngineFormService() {
        return processEngine.getFormEngineFormService();
    }

    public String getName() {
        return processEngine.getName();
    }

    public void close() {
        processEngine.close();
    }
    public RepositoryService getRepositoryService(){
        return processEngine.getRepositoryService();
    }
    public TaskService getTaskService(){
        return processEngine.getTaskService();
    }


}
