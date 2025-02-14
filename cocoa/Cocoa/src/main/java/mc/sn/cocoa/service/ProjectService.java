package mc.sn.cocoa.service;

import java.util.List;
import java.util.Map;

import mc.sn.cocoa.vo.ProjectVO;

public interface ProjectService {

	public int addNewProject(Map projectMap);

	public List listProjects();

	public ProjectVO searchProject(ProjectVO projectVO);

	public void removeProject(int projectNO);
}