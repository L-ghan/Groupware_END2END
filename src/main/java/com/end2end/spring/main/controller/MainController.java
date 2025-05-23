package com.end2end.spring.main.controller;

import com.end2end.spring.approval.service.ApprovalService;
import com.end2end.spring.board.service.NoticeCategoryService;
import com.end2end.spring.employee.dto.DepartmentDTO;
import com.end2end.spring.employee.dto.EmployeeDTO;
import com.end2end.spring.employee.dto.EmployeeDetailDTO;
import com.end2end.spring.employee.dto.JobDTO;
import com.end2end.spring.employee.service.EmployeeService;
import com.end2end.spring.mail.service.MailService;
import com.end2end.spring.main.dto.LoginHistoryDTO;
import com.end2end.spring.main.service.LoginHistoryService;
import com.end2end.spring.schedule.service.ScheduleService;
import com.end2end.spring.util.PageNaviUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class MainController {

	@Autowired private EmployeeService employeeService;
	@Autowired MailService mailService;
	@Autowired private ScheduleService scheduleService;
	@Autowired private LoginHistoryService loginHistoryService;
	@Autowired private NoticeCategoryService noticeCategoryService;
	@Autowired private ApprovalService approvalService;

	@GetMapping("/")
	public String home(HttpSession session, Model model) {
		EmployeeDTO loginUser = (EmployeeDTO) session.getAttribute("employee");
		if (loginUser == null) {
			return "main/login";
		}
		model.addAttribute("birthdayList", employeeService.selectByThisMonthBirthday());
		model.addAttribute("mailReadCount", mailService.getRecordReadCount(loginUser.getId()));
		model.addAttribute("todayScheduleCount", scheduleService.countTodayScheduleByEmployeeId(loginUser.getId()));
		model.addAttribute("noticeCategoryList", noticeCategoryService.selectAll());
		model.addAttribute("approvalList", approvalService.selectRecent("ONGOING", loginUser.getId()));

		return "main/index";
	}

	@RequestMapping("/mypage/{employeeId}")
	public String toMyPage(@PathVariable String employeeId, HttpSession session, Model model) {

		EmployeeDTO loginUser = (EmployeeDTO) session.getAttribute("employee");
		if (loginUser == null) {
			return "redirect:/";
		}

		if (!loginUser.getId().equals(employeeId)
			&& !loginUser.getDepartmentName().equals("인사팀")
			&& !loginUser.getRole().equals("ADMIN")) {
			return "redirect:/";
		}
		EmployeeDetailDTO employee = employeeService.selectDetailById(employeeId);
		model.addAttribute("employeeDetail", employee);

		List<DepartmentDTO> departmentList = employeeService. selectAllDepartment();
		List<JobDTO> jobList = employeeService.selectAllJob();
		model.addAttribute("departmentList", departmentList);
		model.addAttribute("jobList", jobList);
		return "main/mypage";
	}

	@RequestMapping("/login/history")
	public String toLoginHistory(HttpSession session, Model model, int page) {
		EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");

		int totalLength = loginHistoryService.selectByEmployeeId(employee.getId()).size();

		PageNaviUtil.PageNavi pageNavi = new PageNaviUtil(page, totalLength).generate();
		List<LoginHistoryDTO> loginHistoryDTOList =
				loginHistoryService.selectByEmployeeId(employee.getId(), page);

		model.addAttribute("loginHistoryList", loginHistoryDTOList);
		model.addAttribute("pageNavi", pageNavi);

		return "/main/loginHistory";
	}

	@RequestMapping("/contact")
	public String selectContactList(Model model, @RequestParam(defaultValue = "1") int page) {
		List<EmployeeDTO> contactList = employeeService.selectAll();

		PageNaviUtil.PageNavi pageNavi = new PageNaviUtil(page, contactList.size()).generate();
		List<EmployeeDTO> employeeDTOList = employeeService.selectAll(page);

		model.addAttribute("contactList", employeeDTOList);
		model.addAttribute("pageNavi", pageNavi);
		model.addAttribute("url", "/contact?page=");

		return "main/contact";
	}

	@RequestMapping("/contact/search")
	public String searchContactList(@RequestParam(defaultValue = "1") int page, Model model, String searchOption, String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return "redirect:/contact?page=1";
		}

		List<EmployeeDTO> contactList = employeeService.searchContactList(searchOption, keyword);
		PageNaviUtil.PageNavi pageNavi = new PageNaviUtil(page, contactList.size()).generate();
		model.addAttribute("pageNavi", pageNavi);
		model.addAttribute("url", "/contact/search?option=" + searchOption + "&keyword=" + keyword + "page?=");

		model.addAttribute("contactList", employeeService.searchContactList(searchOption, keyword, page));
		return "main/contact";
	}

	@RequestMapping("/test")
	public String toTest() {
		return "works/test";
	}
}