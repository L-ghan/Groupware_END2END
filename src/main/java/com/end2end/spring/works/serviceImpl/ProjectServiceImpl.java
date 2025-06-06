package com.end2end.spring.works.serviceImpl;

import com.end2end.spring.alarm.AlarmService;
import com.end2end.spring.alarm.AlarmType;
import com.end2end.spring.employee.dto.EmployeeDTO;
import com.end2end.spring.util.Statics;
import com.end2end.spring.works.dao.ProjectDAO;
import com.end2end.spring.works.dao.ProjectUserDAO;
import com.end2end.spring.works.dao.ProjectWorkDAO;
import com.end2end.spring.works.dto.*;
import com.end2end.spring.works.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectWorkDAO dao;
    @Autowired
    private ProjectDAO projectDao;
    @Autowired
    private ProjectUserDAO projectUserDao;
    @Autowired
    private AlarmService alarmService;


    //    @Override
//    public Map<String, Integer> getProjectStatistics() {
//        List<ProjectDTO> projects = selectAll();
//        Map<String, Integer> stats = new HashMap<>();
//        // 실제로 projects 데이터를 사용해서 통계 계산
//        int ongoing = 0;
//        int completed = 0;
//
//        for (ProjectDTO project : projects) {
//            if ("진행중".equals(project.getStatus())) {
//                ongoing++;
//            } else if ("완료".equals(project.getStatus())) {
//                completed++;
//            }
//        }
//        stats.put("진행중", ongoing);
//        stats.put("완료", completed);
//        stats.put("전체", projects.size());
//        return stats;
//    }


    @Transactional
    @Override
    public void insert(ProjectInsertDTO dto) {
        ProjectDTO projectDTO = ProjectDTO.builder()
                .name(dto.getTitle())
                .deadLine(dto.getDeadLine())
                .build();
        // 프로젝트 삽입
        projectDao.insert(projectDTO);
//    for(int i = 0 ; i < dto.getEmployeeId().size() ; i++) {
//        ProjectUserDTO projectUserDTO = new ProjectUserDTO(dto.getEmployeeId().get(i), projectDTO.getId());
//        projectUserDao.insert(projectUserDTO);
//    }
        for (String employeeId : dto.getEmployeeId()) {
            ProjectUserDTO projectUserDTO = ProjectUserDTO.builder()
                    .employeeId(employeeId)
                    .projectId(projectDTO.getId())
                    .build();
            projectUserDao.insert(projectUserDTO);
        }

        alarmService.sendProjectAlarm(
                AlarmType.PROJECT_CREATE, "/project/detail/" + projectDTO.getId(), projectDTO.getId());
    }

    @Override
    public ProjectDTO findLatestProject() {
        return projectDao.findLatestProject();
    }

    @Override
    public List<EmployeeDTO> getMembersByProjectId(int projectId) {
        return projectDao.getMembersByProjectId(projectId);
    }
@Override
public ProjectUserDTO selectByProjectIdAndEmployeeId(int projectId,String employeeId){
        return dao.selectByProjectIdAndEmployeeId(projectId,employeeId);
    }
    @Override
    public List<ProjectSelectDTO> selectAllProject() {
        List<ProjectDTO> projectDTO = projectDao.selectAll();
        // SELECT * FROM PROJECT 인데 마감일자 5일 안인지 체크하는 조건도 걸어둠

        List<ProjectSelectDTO> result = new ArrayList<>();
        for (ProjectDTO dto : projectDTO) {
            int id = dto.getId();
            List<EmployeeDTO> projectUserList =
                    projectUserDao.selectByprojectId(id);
            // SELECT EMPLOYEE.* FROM PROJECT_USER JOIN EMPLOYEE ON PROJECT_USER.EMPLOYEEID = EMPLOYEE.ID WHERE PROJECTID = ?

            List<String> profileImgList = new ArrayList<>();
            for (EmployeeDTO employeeDTO : projectUserList) {
                profileImgList.add(employeeDTO.getProfileImg());
            }

            ProjectSelectDTO projectSelectDTO = ProjectSelectDTO.builder()
                    .id(dto.getId())
                    .name(dto.getName())
                    .hideYn(dto.getHideYn())
                    .status(dto.getStatus())
                    .regDate(dto.getRegDate())
                    .deadLine(dto.getDeadLine())
                    .nearDeadline(dto.getNearDeadline())
                    .profileImg(profileImgList)
                    .build();
            result.add(projectSelectDTO);
        }

        return result;
    }
//    public List<ProjectSelectDTO> selectAllProject(int page) {
//        int start = (page - 1) * Statics.recordCountPerPage;
//        int end = Math.min(page * Statics.recordCountPerPage, projectDao.selectAll().size());
//
//        return projectDao.selectAllFromTo(start, end);
//    }

    @Override
    public ProjectDTO selectById(int id) {

        return projectDao.selectById(id);
    }

    @Override
    public void deleteById(int id) {

        projectDao.deleteById(id);
    }

    @Override
    public ProjectInsertDTO update(ProjectInsertDTO dto) {
        // TODO: 프로젝트 수정
        projectDao.update(dto);
        return dto;
    }
    public int hideById(int projectId, String hideYn) {
//        System.out.println("Original hideYn: " + hideYn);

        // 변환 처리
        String convertedHideYn = "false".equalsIgnoreCase(hideYn) ? "N" : "Y";

//        System.out.println("after hideYn: " + convertedHideYn);

        return projectDao.hideById(projectId, convertedHideYn);
    }
    @Override
    public void endworks(int projectId){

        projectDao.endworks(projectId);
    }
    @Override
    public ProjectDTO selectProjectDeadLine(int id)
    {
      return projectDao.selectProjectDeadLine(id);
    }
    @Override
    public void updateProjectUser(int projectId, List<String> employeeId) {
        // 기존 멤버 목록 조회
        List<EmployeeDTO> members = projectUserDao.selectByprojectId(projectId);

        for (EmployeeDTO member : members) {
            boolean isDuplicate = false;
            for (String employee : employeeId) {
                if (employee.equals(member.getId()))
                    isDuplicate = true;
                break;
            }
            if(!isDuplicate){
                projectUserDao.deleteMemberById(projectId,member.getId());
            }
        }
        for (String employee : employeeId) {
            boolean isDuplicat = false;
            for (EmployeeDTO member : members) {
                if (employee.equals(member.getId()))
                    isDuplicat = true;
                break;
            }

            if(!isDuplicat){
                projectUserDao.insertProjectMember(projectId,employee);
            }

        }
        //있는 멤버인지 확인후 없으면 추가
    }

    @Override
    public void updateProject(ProjectInsertDTO dto) {
        projectDao.update(dto);

    }


    @Override
    public List<ProjectDTO> selectByName(String name) {
        // TODO: 이름으로 검색
        return null;
    }

    @Override
    public List<EmployeeDTO> selectByUser(String name) {
        // TODO: 이름으로 검색
        String target = "%" + name + "%";
//System.out.println("selectByUser"+target);
        return projectDao.selectByUser(target);
    }


}
