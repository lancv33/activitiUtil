package com.dhcc.act.utils;

import com.dhcc.act.bean.LocalUserTask;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ActUtils {
    public ActUtils(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    private ProcessEngine processEngine;


    /**
     * @author lht
     * @doc   结束流程（一般用于拒绝流程使用）
     * @date 2018/6/8
     * @param taskId
     * @return
     */
    public void endProcess(String taskId){
        Task task=processEngine.getTaskService().createTaskQuery()// 创建任务查询
                .taskId(taskId) // 根据任务id查询
                .singleResult();
        processEngine.getRuntimeService().deleteProcessInstance(task.getProcessInstanceId(),null);
    }

    /**
     * @author lht
     * @doc   获得流程图
     * @date 2018/6/8
     * @param processInstanceId
     * @return
     */
    public InputStream getActivitiProccessImage(String processInstanceId){
        return getActivitiProccessImage(processInstanceId,processEngine.getHistoryService(),processEngine.getProcessEngineConfiguration(),processEngine.getRepositoryService());
    }




    /**
     * @author lht
     * @doc   获得流程图内部方法
     * @date 2018/6/8
     * @param pProcessInstanceId
    * @param historyService
    * @param processEngineConfiguration
    * @param repositoryService
     * @return
     */
    private InputStream getActivitiProccessImage(String pProcessInstanceId, HistoryService historyService, ProcessEngineConfiguration processEngineConfiguration, RepositoryService repositoryService) {
        InputStream imageStream = null;
        try {
            //  获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(pProcessInstanceId).singleResult();

            if (historicProcessInstance == null) {
                throw new Exception("获取流程实例ID[" + pProcessInstanceId + "]对应的历史流程实例失败！");
            }else{
                // 获取流程定义
                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());

                // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(pProcessInstanceId).orderByHistoricActivityInstanceId().asc().list();

                // 已执行的节点ID集合
                List<String> executedActivityIdList = new ArrayList<String>();
                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                    executedActivityIdList.add(activityInstance.getActivityId());
                }

                BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());

                // 已执行的线集合
                List<String> flowIds ;
                // 获取流程走过的线 (getHighLightedFlows是下面的方法)
                flowIds = getHighLightedFlows(bpmnModel,processDefinition, historicActivityInstanceList);

                // 获取流程图图像字符流
                ProcessDiagramGenerator pec = processEngineConfiguration.getProcessDiagramGenerator();
                //配置字体
                imageStream = pec.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds,"宋体","微软雅黑","黑体",null,2.0);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(imageStream!=null){
                    imageStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return imageStream;
    }
    /**
     * @author lht
     * @doc   获得流程图内部方法，获得已执行流程实例的流程线
     * @date 2018/6/8
     * @param bpmnModel
    * @param processDefinitionEntity
    * @param historicActivityInstances
     * @return
     */
    private static List<String> getHighLightedFlows(BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //24小时制
        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId

        for (int i = 0; i < historicActivityInstances.size() - 1; i++)
        {
            // 对历史流程节点进行遍历
            // 得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode)bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());


            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();// 用以保存后续开始时间相同的节点
            FlowNode sameActivityImpl1 = null;

            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);// 第一个节点
            HistoricActivityInstance activityImp2_ ;

            for(int k = i + 1 ; k <= historicActivityInstances.size() - 1; k++)
            {
                activityImp2_ = historicActivityInstances.get(k);// 后续第1个节点

                if ( activityImpl_.getActivityType().equals("userTask") && activityImp2_.getActivityType().equals("userTask") &&
                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))   ) //都是usertask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
                {

                }
                else
                {
                    sameActivityImpl1 = (FlowNode)bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());//找到紧跟在后面的一个节点
                    break;
                }

            }
            sameStartTimeNodes.add(sameActivityImpl1); // 将后面第一个节点放在时间相同节点的集合里
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++)
            {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点

                if (df.format(activityImpl1.getStartTime()).equals(df.format(activityImpl2.getStartTime()))  )
                {// 如果第一个节点和第二个节点开始时间相同保存
                    FlowNode sameActivityImpl2 = (FlowNode)bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                }
                else
                {// 有不相同跳出循环
                    break;
                }
            }
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows() ; // 取出节点的所有出去的线

            for (SequenceFlow pvmTransition : pvmTransitions)
            {// 对所有的线进行遍历
                FlowNode pvmActivityImpl = (FlowNode)bpmnModel.getMainProcess().getFlowElement( pvmTransition.getTargetRef());// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }

        }
        return highFlows;

    }


    /**
     * @Author: lht
     * @Description: 获得流程的需要设置参数 递归方法
     * @Date: 2019/1/11 17:12
     * @param operaCode
     * @return: void
     */
    public Iterator<String>  getProcessParamById(String operaCode){
        //开启流程之前先寻找最新版本的请假流程定义
        ProcessDefinitionQuery query = processEngine.getRepositoryService().createProcessDefinitionQuery();
        //添加查询条件，KEY
        ProcessDefinition definition = query.processDefinitionKey(operaCode).latestVersion().singleResult();
        //获得流程模型
        BpmnModel d= processEngine.getRepositoryService().getBpmnModel(definition.getId());
        Collection<FlowElement> flowElementList = d.getProcesses().get(0).getFlowElements();
        //所有审批节点参数（临时，还未筛选）
        List<String> usersList = new ArrayList<String>();
        //子流程 用户编码数据
        List<String> childrensUsers = new ArrayList<>();
        //遍历节点 ，获得所有审批节点参数
        for (FlowElement flowElement:flowElementList){
            if(flowElement instanceof UserTask){
                List<String> candidateUsers = ((UserTask) flowElement).getCandidateUsers();
                List<String> candidateGroups = ((UserTask) flowElement).getCandidateGroups();
                String assignee = ((UserTask) flowElement).getAssignee();
                if(candidateUsers.size()>0){
                    usersList.addAll(candidateUsers);
                }
                if(candidateGroups.size()>0){
                    usersList.addAll(candidateGroups);
                }
                if(!StringUtils.isEmpty(assignee)){
                    usersList.add(assignee);
                }
            }else if(flowElement instanceof CallActivity){
                List<IOParameter> ioParameters = ((CallActivity) flowElement).getInParameters();
                Iterator<String> childrensUsersTemp =getProcessParamById(((CallActivity) flowElement).getCalledElement());
                //对比入参，判断是否需要设置值
                while(childrensUsersTemp.hasNext()){
                    String pureChildrensUser = childrensUsersTemp.next();
                    for(IOParameter ioParameter : ioParameters){
                        //命中目标
                        if(ioParameter.getTarget().equals(pureChildrensUser)){
                            childrensUsers.add(ioParameter.getSource());
                        }
                    }
                }
            }
        }

        //创建所有流程的用户编码数据容器
        Set<String> resultUserCode = new HashSet<>();
        //对本次流程做纯净处理
        for(String userCode : usersList){
            String[] usersCodeTemp = getPureCodes(userCode);
            if(usersCodeTemp!=null){
                resultUserCode.addAll(Arrays.asList(usersCodeTemp));
            }
        }
        resultUserCode.addAll(childrensUsers);
        return resultUserCode.iterator();

    }




    /**
     * @Author: lht
     * @Description: 去除${},取出纯净code
     * @Date: 2018/11/14 14:58
     * @param code
     * @return: java.lang.String[]
     */
    public String[] getPureCodes(String code){
        if(StringUtils.isEmpty(code)){
            return new String[0];
        }
        //去空
        String userTemp = code.replaceAll(" ","");
        //筛选
        int start = userTemp.indexOf("${");
        int end = userTemp.indexOf("}",start);
        if(start>-1&&end>-1){
            //截取
            String userParamsStr = userTemp.substring((start+2),end);
            return userParamsStr.split(",");
        }else{
            return null;
        }
    }


    /**
     * @Author: lht
     * @Description: 返回自定义封装的UserTask
     * @Date: 2018/11/14 14:29
     * @param processInstanceId
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     */
    public List<LocalUserTask> getProcessModal(String processInstanceId){
        Map<String,Object> resultMap  = new HashMap<>();
        //根据流程id获得流程模式id
        String processDefinitionId  = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
        //获得流程模型
        BpmnModel model = processEngine.getRepositoryService().getBpmnModel(processDefinitionId);
        //获得流程模型的所有节点
        Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
        //保存userTask信息
        List<UserTask> userList = new ArrayList<>();
        for(FlowElement f : flowElements){
            if(f instanceof StartEvent){
                try{
                    getNextUser(flowElements,f.getId(),userList);
                }catch (IOException e){
                    break;
                }
            }
        }
        //当前任务
        List<Task> tasks = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        //当前任务modal的id集合
        List<String> currentTask = new ArrayList<String>();
        for(Task task :tasks){
            currentTask.add(task.getTaskDefinitionKey());
        }

        //分解数据，装入bean中
        List<LocalUserTask> localUserTasks = new ArrayList<LocalUserTask>();
        for(UserTask userTask:userList){
            LocalUserTask localUserTask = new LocalUserTask();
            //判断是否为当前任务节点,默认为false
            if(currentTask.contains(userTask.getId())){
                localUserTask.setCurrent(true);
            }
            //装入纯净CandidateUser
            List<String> pureCandidateUsers = localUserTask.getPureCandidateUsers();
            for(String pureCandidateUser :userTask.getCandidateUsers()){
                pureCandidateUsers.addAll(Arrays.asList(getPureCodes(pureCandidateUser)));
            }
            //装入纯净CandidateGroups
            List<String> pureCandidateGroups = localUserTask.getPureCandidateGroups();
            for(String pureCandidateGroup :userTask.getCandidateGroups()){
                pureCandidateGroups.addAll(Arrays.asList(getPureCodes(pureCandidateGroup)));
            }
            localUserTask.setFlowElements(flowElements);
            localUserTask.setName(userTask.getName());
            localUserTask.setUserTask(userTask);
            localUserTasks.add(localUserTask);
        }
        return localUserTasks;

    }


    /**
     * @Author: lht
     * @Description: 获得所有审批人的递归方法
     * @Date: 2018/11/8 11:23
     * @param flowElements
     * @param source
     * @param userList
     * @return: void
     */
    private void getNextUser(Collection<FlowElement> flowElements,String source,List<UserTask> userList) throws IOException {

        String targetRef = null;
        for(FlowElement f :flowElements){
            if(f instanceof SequenceFlow&&((SequenceFlow) f).getSourceRef().equals(source)){
                targetRef = ((SequenceFlow) f).getTargetRef();
            }
        }
        if(targetRef!=null){
            for(FlowElement f :flowElements){
                if(targetRef.equals(f.getId())){
                    if(f instanceof EndEvent){
                        throw new IOException("跳出递归");
                    }else if(f instanceof UserTask){
                        userList.add((UserTask)f);
                        getNextUser(flowElements,f.getId(),userList);
                    }else{
                        getNextUser(flowElements,f.getId(),userList);
                    }
                }
            }
        }
        System.out.println("source:"+source);
        throw new RuntimeException("没有找到线");
    }

}
