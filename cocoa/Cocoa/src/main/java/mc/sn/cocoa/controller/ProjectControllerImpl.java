package mc.sn.cocoa.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import mc.sn.cocoa.service.ProjectService;
import mc.sn.cocoa.vo.MemberVO;
import mc.sn.cocoa.vo.ProjectVO;
import net.coobird.thumbnailator.Thumbnails;

@Controller("projectController")
public class ProjectControllerImpl implements ProjectController {

	// 저장되는 경로
	private static final String project_IMAGE_REPO = "C:\\cocoa\\project_image";
	@Autowired
	private ProjectService projectService;

	// 프로젝트 글 작성 창으로 이동
	@Override
	@RequestMapping(value = "/view_projectWrite", method = RequestMethod.GET)
	public ModelAndView view_projectWrite(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		String url = "/projectWriteForm";
		mav.setViewName(url);
		return mav;
	}

	@Override
	@RequestMapping(value = "/view_projectInfo", method = RequestMethod.GET)
	public ModelAndView projectInfo(@RequestParam("id") String id, @RequestParam("projectNO") int projectNO,
			HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		request.setCharacterEncoding("utf-8");
		ModelAndView mav = new ModelAndView();
		ProjectVO projectVO = new ProjectVO();
		projectVO.setLeader(id);
		projectVO.setProjectNO(projectNO);
		ProjectVO vo = projectService.searchProject(projectVO);
		mav.addObject("projectInfo", vo);
		String url = "/projectInfo";
		mav.setViewName(url);
		return mav;
	}

	// 프로젝트 글 작성
	@Override
	@RequestMapping(value = "/projectWrite", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity addNewProject(MultipartHttpServletRequest multipartRequest, HttpServletResponse response)
			throws Exception {

		multipartRequest.setCharacterEncoding("utf-8");

		// DB에 담을 프로젝트 글 정보
		Map<String, Object> projectMap = new HashMap<String, Object>();

		// 받아온 정보들을 projectMap에 [key & value]로 설정
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			String value = multipartRequest.getParameter(name);
			projectMap.put(name, value);
		}

		// 세션 불러오기
		HttpSession session = multipartRequest.getSession();

		// memberVO로 세션에 저장된 로그인한 회원의 정보를 저장
		MemberVO memberVO = (MemberVO) session.getAttribute("member");

		// projectWriteForm에 존재하지 않는 id 직접 입력
		String id = memberVO.getId();
		projectMap.put("leader", id);
		// projectWriteForm에 불러온 파일(이미지) 직접 입력
		String pImg = this.upload(multipartRequest);
		projectMap.put("pImg", pImg);

		// 성공, 실패 시 알림
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");

		try {
			// 다운로드 파일을 작성하는 회원의 id(leader)로 폴더 생성
			// 삭제가 되어도 그 후에 해당 회원이 업로드를 하면 같은 폴더에 생성이 됌
			// 삭제 로직 짤때 고려해야함
			int projectNO = projectService.addNewProject(projectMap);
			if (pImg != null && pImg.length() != 0) {
				File srcFile = new File(project_IMAGE_REPO + "\\" + "temp" + "\\" + pImg);
				File destDir = new File(project_IMAGE_REPO + "\\" + id + "\\" + projectNO);
				FileUtils.moveFileToDirectory(srcFile, destDir, true);
			}

			message = "<script>";
			message += " alert('등록이 완료되었습니다.');";
			message += " location.href='" + multipartRequest.getContextPath() + "/'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

		} catch (Exception e) {

			// 예외발생시 취소 및 삭제
			File srcFile = new File(project_IMAGE_REPO + "\\" + "temp" + "\\" + pImg);
			srcFile.delete();

			message = " <script>";
			message += " alert('오류가 발생했습니다. 다시 시도해주세요.');');";
			message += " location.href='" + multipartRequest.getContextPath() + "/'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;
	}

	@Override
	@RequestMapping(value = "/removeProject", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity removeProject(@RequestParam("projectNO") int projectNO, @RequestParam("leader") String id,
			HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html; charset=UTF-8");
		System.out.println(projectNO + "," + id);
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			projectService.removeProject(projectNO);
			File destDir = new File(project_IMAGE_REPO + "\\" + id + "\\" + projectNO);
			FileUtils.deleteDirectory(destDir);

			message = "<script>";
			message += " alert('프로젝트 게시글을 삭제하였습니다');";
			message += " location.href='" + request.getContextPath() + "/';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

		} catch (Exception e) {
			message = "<script>";
			message += " alert('삭제에 실패했습니다');";
			message += " location.href='" + request.getContextPath() + "/';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;

	}

	// 파일 업로드
	private String upload(MultipartHttpServletRequest multipartRequest) throws Exception {

		String pImg = null;
		Iterator<String> fileNames = multipartRequest.getFileNames();

		while (fileNames.hasNext()) {

			String fileName = fileNames.next();
			MultipartFile mFile = multipartRequest.getFile(fileName);
			pImg = mFile.getOriginalFilename();

			File file = new File(project_IMAGE_REPO + "\\" + "temp" + "\\" + fileName);

			if (mFile.getSize() != 0) {
				if (!file.exists()) {
					if (file.getParentFile().mkdirs()) {
						file.createNewFile();
					}
				}
				mFile.transferTo(new File(project_IMAGE_REPO + "\\" + "temp" + "\\" + pImg));
			}
		}
		return pImg;
	}

	// 이미지파일 썸네일로 다운로드
	@RequestMapping("/thumbnails")
	// RequestParam으로 key&value 값을 가져와 변수에 저장
	protected void thumbnails(@RequestParam("pImg") String pImg, @RequestParam("leader") String leader,
			@RequestParam("projectNO") String projectNO, HttpServletResponse response) throws Exception {
		OutputStream out = response.getOutputStream();
		// 파일 경로
		String filePath = project_IMAGE_REPO + "\\" + leader + "\\" + projectNO + "\\" + pImg;
		File image = new File(filePath);

		if (image.exists()) {
			// 원본 이미지에 대한 썸네일 이미지를 생성한 후 OutputStream 객체에 할당
			Thumbnails.of(image).size(1024, 1024).outputFormat("png").toOutputStream(out);
		}
		// 썸네일 이미지를 OutputStream 객체를 이용해 브라우저로 전송
		byte[] buffer = new byte[1024 * 8];
		out.write(buffer);
		out.close();
	}

	@RequestMapping("/download")
	protected void download(@RequestParam("pImg") String pImg, @RequestParam("leader") String leader,
			@RequestParam("projectNO") String projectNO, HttpServletResponse response) throws Exception {
		OutputStream out = response.getOutputStream();
		String filePath = project_IMAGE_REPO + "\\" + leader + "\\" + projectNO + "\\" + pImg;
		File image = new File(filePath);

		response.setHeader("Cache-Control", "no-cache");
		response.addHeader("Content-disposition", "attachment; fileName=" + pImg);
		FileInputStream in = new FileInputStream(image);
		byte[] buffer = new byte[1024 * 8];
		while (true) {
			int count = in.read(buffer);
			if (count == -1)
				break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
	}
}