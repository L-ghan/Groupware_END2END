package com.end2end.spring.commute.controller;

import com.end2end.spring.commute.dto.CommuteDTO;
import com.end2end.spring.commute.dto.SolderingDTO;
import com.end2end.spring.commute.service.CommuteService;
import com.end2end.spring.commute.service.SolderingService;
import com.end2end.spring.commute.service.VacationService;
import com.end2end.spring.employee.dto.EmployeeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@RequestMapping("/commute")
@Controller
public class CommuteController {
    @Autowired private CommuteService commuteService;
    @Autowired private SolderingService solderingService;
    @Autowired private VacationService vacationService;

    @RequestMapping("/detail/{employeeId}")
    public String toDetail(@PathVariable int employeeId, HttpSession session, Model model) {
        // TODO: 해당 id의 사원의 detail.jsp로 이동
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        CommuteDTO commuteDTO = CommuteDTO.builder()
                .employeeId(employee.getId())
                .build();

        commuteDTO.setState("WORK_ON");
        CommuteDTO workOnTime = commuteService.selectByStateAndEmployeeId(commuteDTO);
        if(workOnTime != null) {
            model.addAttribute("workOnTime", workOnTime.getRegDate());
        }

        commuteDTO.setState("WORK_OFF");
        CommuteDTO workOffTime = commuteService.selectByStateAndEmployeeId(commuteDTO);
        if(workOffTime != null) {
            model.addAttribute("workOffTime", workOffTime.getRegDate());
        }

        model.addAttribute("workOnCount", commuteService.countWorkOnThisWeekByEmployeeId(employee.getId()));

        SolderingDTO solderingDTO = SolderingDTO.builder()
                .employeeId(employee.getId())
                .build();
        solderingDTO.setState("ABSENCE");
        model.addAttribute("absenceCount", solderingService.countTisWeekByStateAndEmployeeId(solderingDTO));

        solderingDTO.setState("LATE");
        model.addAttribute("lateCount", solderingService.countTisWeekByStateAndEmployeeId(solderingDTO));

        solderingDTO.setState("LEAVE_EARLY");
        model.addAttribute("leaveEarlyCount", solderingService.countTisWeekByStateAndEmployeeId(solderingDTO));

        model.addAttribute("totalVacationDates", vacationService.sumTotalVacationDates(employee.getId()));
        model.addAttribute("totalUsedVacationDates", vacationService.sumTotalUsedVacationDates(employee.getId()));
        model.addAttribute("thisMonthUsedVacationDates", vacationService.sumThisMonthUsedVacationDates(employee.getId()));

        return "commute/detail";
    }

    @RequestMapping("/list/{departmentId}")
    public String toList(@PathVariable int departmentId, Model model) {
        // TODO: 해당 id의 부서의 list.jsp로 이동
        return "commute/list";
    }

    @ResponseBody
    @RequestMapping("/workOn")
    public boolean workOn(HttpSession session) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        boolean workOn = commuteService.workOn(employee.getId());

        session.setAttribute("isWorkOn", workOn);
        return workOn;
    }

    @ResponseBody
    @RequestMapping("/workOff")
    public boolean workOff(HttpSession session) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        boolean workOff = commuteService.workOff(employee.getId());

        session.setAttribute("isWorkOff", workOff);
        return workOff;
    }

    @ResponseBody
    @RequestMapping("/leaveEarly/test")
    public void leaveEarly(HttpSession session) {
        solderingService.insertByState("LATE");
    }
}
