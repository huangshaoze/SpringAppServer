package org.springframework.samples.mvc.simple;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.samples.mvc.jpa.entity.News;
import org.springframework.samples.mvc.jpa.entity.User;
import org.springframework.samples.mvc.jpa.service.NewsService;
import org.springframework.samples.mvc.jpa.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.DateFormatUtil;
import org.springframework.web.util.HttpRequestUtils;

/**
 * @author Arison
 *
 */
@Controller("SimpleController")
public class SimpleController {
	@Resource
	private UserService userService;

	@Resource
	private NewsService newsService;

	/**
	 * ��ȡ�ͻ�������ͷ��Ϣ ip��ַ,����ͷ��Ϣ,����,����ȵ�
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/client/info")
	public @ResponseBody Map<String, Object> getClientBaseInfo(
			HttpServletRequest request,
			HttpServletResponse response,
			@CookieValue(value = "JSESSIONID", required = false) String sessionId) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> resultBody = new HashMap<String, Object>();
		String ipAddress = getIPAddress(request);// ��ȡ�ͻ���ip��ַ
		Enumeration headerNames = request.getHeaderNames();
		request.getHeader("sessionUser"); 
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			result.put(key, value);
		}
		request.getHeader("cache-control");

		boolean isMobile = HttpRequestUtils.isMobileDevice(request);
		resultBody.put("isMoblie", isMobile);
		resultBody.put("headers", result);
		resultBody.put("JSESSIONID", sessionId);
		resultBody.put("ip", ipAddress);
		HttpSession httpSession = request.getSession();
		httpSession.setMaxInactiveInterval(1800);
		resultBody.put("sessionId", httpSession.getAttribute("sessionId"));
		if (httpSession != null
				&& !StringUtils.isEmpty(httpSession.getAttribute("sessionId"))) {
			if (httpSession.getAttribute("sessionId").equals(sessionId)) {
				// ��½����״̬
				resultBody.put("loginState", "��½״̬�����ߣ�");

			} else {
				// ��¼�Ͽ�״̬
				resultBody.put("loginState", "�Ự�Ͽ���");
			//	httpSession.setAttribute("sessionId", httpSession.getId());
			}
		} else {
			// ��¼�Ͽ�״̬
			resultBody.put("loginState", "�Ự�Ͽ���");
			//httpSession.setAttribute("sessionId", httpSession.getId());
		}

		resultBody.put("MaxInactiveInterval",
				httpSession.getMaxInactiveInterval());
		resultBody
				.put("LastAccessedTime", DateFormatUtil
						.getFormatDate(httpSession.getLastAccessedTime()));
		resultBody.put("CreationTime",
				DateFormatUtil.getFormatDate(httpSession.getCreationTime()));
		resultBody.put("isNew", httpSession.isNew());
		resultBody.put("HttpSessionId", httpSession.getId());
        /*  try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		return resultBody;
	}

	
	/**ģ��ͻ��˵�½���ѵ�ǰ�ỰIDд�������request cookie
	 * @param request
	 * @param sessionId
	 * @return
	 */
	@RequestMapping(value = "/client/login")
	public String  loginSession(
			HttpServletRequest request,
			@CookieValue(value = "JSESSIONID", required = false) String sessionId) {
		Map<String, Object> result = new HashMap<String, Object>();
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("sessionId", httpSession.getId());
		result.put("loginState", "��½�ɹ���");
		result.put("sessionId", httpSession.getId());
		return "me/index";
	}
	
	
	@RequestMapping(value = "/client/logout")
	public String logoutSession(
			HttpServletRequest request,
			@CookieValue(value = "JSESSIONID", required = false) String sessionId) {
		Map<String, Object> result = new HashMap<String, Object>();
		HttpSession httpSession = request.getSession();
		httpSession.invalidate();
		result.put("loginState", "ע����½�ɹ���");
		return "me/index";
	}
	

	/**
	 * ɾ��cookie
	 * 
	 * @param request
	 * @param response
	 * @param name
	 */
	@RequestMapping("/delCookie")
	public @ResponseBody Map<String, Object> delCookie(
			@CookieValue(value = "JSESSIONID", required = false) String sessionId,
			HttpServletRequest request, HttpServletResponse response,
			String name) {
		Map<String, Object> result = new HashMap<String, Object>();
		Cookie[] cookies = request.getCookies();
		if (null == cookies) {
			System.out.println("û��cookie==============");
		} else {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					cookie.setValue(null);
					cookie.setMaxAge(0);// ��������cookie
					cookie.setPath("/");
					System.out.println("��ɾ����cookie����Ϊ:" + cookie.getName()
							+ ";path:" + cookie.getPath());
					request.getSession().removeAttribute("sessionId");
					// request.getSession().invalidate();
					// response.addCookie(cookie);
					break;
				}
			}
		}

		result.put("JSESSIONID", sessionId);
		result.put("sessionId", request.getSession().getAttribute("sessionId"));
		return result;
	}

	/**
	 * ��ȡ����cookie ע������ӿͻ��˶�ȡCookieʱ������maxAge���ڵ��������Զ��ǲ��ɶ��ģ�Ҳ���ᱻ�ύ��
	 * ������ύCookieʱֻ���ύname��value���ԡ�maxAge����ֻ������������ж�Cookie�Ƿ����
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/showCookies")
	public @ResponseBody Map<String, Object> showCookies(
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<String, Object>();
		Cookie[] cookies = request.getCookies();// ��������Ի�ȡһ��cookie����
		String sessionId = (String) request.getSession().getAttribute(
				"sessionId");
		if (null == cookies) {
			System.out.println("û��cookie=========");
		} else {
			for (Cookie cookie : cookies) {
				System.out.println("name:" + cookie.getName() + ",value:"
						+ cookie.getValue());
				result.put(cookie.getName(), cookie.getValue());
			}
		}
		result.put("sessionId", sessionId);
		return result;

	}

	private String getIPAddress(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		ip = request.getHeader("X-Forwarded-For");
		if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
			// ��η���������ж��IPֵ����һ��Ϊ��ʵIP��
			int index = ip.indexOf(',');
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		} else {
			return request.getRemoteAddr();
		}
	}

	@RequestMapping(value = "/simple/save/{name}/{password}", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	public String simpleSave(@PathVariable String name,
			@PathVariable String password) {
		User user = new User();
		user.setUsername(name);
		user.setPassword(password);
		userService.saveUser(user);
		User user2 = userService.findOneUser(name, password);

		News news = new News();
		news.setTitle("����������");
		news.setContent("�����ຣ");
		news.setUserId(user2.getId());
		// news.setUserId(user2);
		newsService.save(news);
		// "����ɹ�����ѯ�ӿ�  localhost:8080/spring-mvc-showcase/simple/find/"+user2.getId();
		return "redirect:/simple/find/" + user2.getId();
	}

	@RequestMapping(value = "/simple1/save/", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	public Object simple1Save(@RequestParam String name,
			@RequestParam String password) {
		User user2 = new User();
		try {
			User user = new User();
			user.setUsername(name);
			user.setPassword(password);
			userService.saveUser(user);
			user2 = userService.findOneUser(name, password);

			News news = new News();
			news.setTitle("����������");
			news.setContent("�����ຣ");
			news.setUserId(user2.getId());
			// news.setUserId(user2);
			newsService.save(news);
		} catch (Exception e) {
			System.out.println("�����ڲ��쳣��");
		}
		// "����ɹ�����ѯ�ӿ�  localhost:8080/spring-mvc-showcase/simple/find/"+user2.getId();
		return "redirect:/simple/find/" + user2.getId();
	}

	@RequestMapping(value = "/simple/find/{id}", method = RequestMethod.POST)
	public @ResponseBody Object simpleFind(@PathVariable Long id) {
		User user = new User();
		try {
			user = userService.findOneUser(id);
		} catch (Exception e) {
			return "�����쳣��";

		}
		return user;
	}

	@RequestMapping(value = "/simple", produces = "text/plain;charset=utf-8")
	public @ResponseBody String simple() {
		return "Hello world!";
	}

	@RequestMapping(value = "/simple/map")
	public @ResponseBody Map<String, Object> map() {
		Map<String, Object> map = new HashMap<>();
		List<User> users = userService.findAllUsers();
		for (int i = 0; i < users.size(); i++) {
			System.out.println(users.get(i).getUsername());
			map.put("users", users);
			map.put("name", users.get(i).getUsername());
			map.put("password", users.get(i).getPassword());
		}

		return map;
	}

	@RequestMapping(value = "/index1", method = RequestMethod.GET)
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView("/user/index");
		modelAndView.addObject("name", "xxx");
		return modelAndView;
	}

	// ����ModelAndView���캯������ָ������ҳ������ƣ�Ҳ����ͨ��setViewName��������������Ҫ��ת��ҳ�棻

	@RequestMapping(value = "/index2", method = RequestMethod.GET)
	public ModelAndView index2() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("name", "xxx");
		modelAndView.setViewName("/user/index");
		return modelAndView;
	}

	/**
	 * Modelһ��ģ�Ͷ��� ��Ҫ����spring��װ�õ�model��modelMap,�Լ�java.util.Map��
	 * ��û����ͼ���ص�ʱ����ͼ���ƽ���requestToViewNameTranslator������
	 * 
	 * @return
	 */
	@RequestMapping(value = "/index3", method = RequestMethod.GET)
	public Map<String, String> index3() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("1", "1");
		// map.put�൱��request.setAttribute����
		return map;
	}

	// ����String
	// ͨ��model����ʹ��
	@RequestMapping(value = "/index4", method = RequestMethod.GET)
	public @ResponseBody String index(Model model) {

		return "jsp";
	}

}
