package mc.sn.cocoa.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mc.sn.cocoa.dao.ProjectDAO;
import mc.sn.cocoa.vo.ProjectVO;

@Service("projectService")
@Transactional(propagation = Propagation.REQUIRED)
public class ProjectServiceImpl implements ProjectService {
	@Autowired
	private ProjectDAO projectDAO;

	// 새 프로젝트 글 삽입
	@Override
	public int addNewProject(Map projectMap) {
		return projectDAO.insertNewProject(projectMap);
	}

	// 프로젝트 리스트 가져오기
	@Override
	public List listProjects() {
		List projectsList = null;
		projectsList = projectDAO.selectAllProjectList();
		return projectsList;
	}

	@Override
	public ProjectVO searchProject(ProjectVO projectVO) {
		ProjectVO vo = null;
		vo = projectDAO.selectProjectById(projectVO);
		return vo;
	}

	@Override
	public void removeProject(int projectNO) {
		projectDAO.deleteProject(projectNO);
	}
}