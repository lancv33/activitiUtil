package com.dhcc.act.bean;

import de.odysseus.el.util.SimpleContext;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Auther: lht
 * @Date: 2018/11/14 14:35
 * @Description: 封装activitiuserTask
 */
public class LocalUserTask {
    private UserTask userTask;
    private Collection<FlowElement> flowElements;
    private boolean isCurrent = false;
    private List<String> pureCandidateUsers = new ArrayList<String>();
    private List<String> pureCandidateGroups = new ArrayList<String>();
    private String name;

    public UserTask getUserTask() {
        return userTask;
    }

    public void setUserTask(UserTask userTask) {
        this.userTask = userTask;
    }

    public Collection<FlowElement> getFlowElements() {
        return flowElements;
    }

    public void setFlowElements(Collection<FlowElement> flowElements) {
        this.flowElements = flowElements;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public List<String> getPureCandidateUsers() {
        return pureCandidateUsers;
    }

    public void setPureCandidateUsers(List<String> pureCandidateUsers) {
        this.pureCandidateUsers = pureCandidateUsers;
    }

    public List<String> getPureCandidateGroups() {
        return pureCandidateGroups;
    }

    public void setPureCandidateGroups(List<String> pureCandidateGroups) {
        this.pureCandidateGroups = pureCandidateGroups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
