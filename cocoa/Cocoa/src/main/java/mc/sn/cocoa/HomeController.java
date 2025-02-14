package mc.sn.cocoa;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import mc.sn.cocoa.service.ProjectService;

@Controller
public class HomeController {
	@Autowired
	private ProjectService projectService;

	// 홈화면 이동하면서 프로젝트 카탈로그 리스트 가져오기
	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView home(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView();
		
		// 서비스에서 listProjects() 메소드 실행하여 리턴 값을 List타입의 projectList에 저장
		List projectList = projectService.listProjects();
		
		// mav에 "projectList" 키값으로 projectList 밸류 값을 저장
		mav.addObject("projectList", projectList);
		
		String url = "/home";
		mav.setViewName(url);
		
		return mav;
	}
}