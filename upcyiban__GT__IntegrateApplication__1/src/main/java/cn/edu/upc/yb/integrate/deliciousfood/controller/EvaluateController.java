package cn.edu.upc.yb.integrate.deliciousfood.controller;

import cn.edu.upc.yb.integrate.common.dto.ErrorReporter;
import cn.edu.upc.yb.integrate.common.dto.YibanBasicUserInfo;
import cn.edu.upc.yb.integrate.common.service.AppAdminService;
import cn.edu.upc.yb.integrate.common.service.CommonAdminService;
import cn.edu.upc.yb.integrate.deliciousfood.dao.VarietyOfDishesDao;
import cn.edu.upc.yb.integrate.deliciousfood.model.VarietyOfDishes;
import cn.edu.upc.yb.integrate.deliciousfood.service.UploadService;
import cn.edu.upc.yb.integrate.deliverwater.dto.JsonMes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

/**
 * Created by 陈子枫 on 2017/2/6.
 * 用于写菜品评价的接口
 */

@RestController
@RequestMapping(value = "/evaluate")
public class EvaluateController {

	@Autowired
	private VarietyOfDishesDao varietyOfDishesDao;

	@Autowired
	private CommonAdminService commonAdminService;

	@Autowired
	AppAdminService appAdminService;

	@Autowired
	HttpSession httpSession;

	@Autowired
	UploadService uploadService;

	@RequestMapping(method = RequestMethod.POST, value = "/create")
	public Object create(String name, String region, String kind, String restaurant, String price, String introduce,
			MultipartFile file) {
		if (httpSession.getAttribute("user") == null)
			return new cn.edu.upc.yb.integrate.deliciousfood.dto.JsonMes(0, "请先登录");
		YibanBasicUserInfo yibanBasicUserInfo = (YibanBasicUserInfo) httpSession.getAttribute("user");
		int ybid = yibanBasicUserInfo.visit_user.userid;
		//管理员验证
		if (!appAdminService.isAppAdmin("deliciousfood", yibanBasicUserInfo.visit_user.userid))
			return new ErrorReporter(-1, "您不是管理员");
		VarietyOfDishes varietyOfDishes = new VarietyOfDishes(name, region, kind, restaurant, price, introduce);
		varietyOfDishesDao.save(varietyOfDishes);
		uploadService.storePhoto(file, varietyOfDishes.getId(), ybid);

		return new JsonMes(1, "创建成功");
	}

	@RequestMapping("/update")
	public Object update(int id, String price) {
		YibanBasicUserInfo yibanBasicUserInfo = (YibanBasicUserInfo) httpSession.getAttribute("user");
		//管理员验证
		if (!appAdminService.isAppAdmin("deliciousfood", yibanBasicUserInfo.visit_user.userid))
			return new ErrorReporter(-1, "您不是管理员");
		VarietyOfDishes varietyOfDishes = varietyOfDishesDao.findOne(id);
		varietyOfDishes.setPrice(price);
		varietyOfDishesDao.save(varietyOfDishes);
		return new JsonMes(1, "更改成功");
	}

	@RequestMapping("/delete")
	public Object delete(int id) {
		YibanBasicUserInfo yibanBasicUserInfo = (YibanBasicUserInfo) httpSession.getAttribute("user");
		//管理员验证
		if (!appAdminService.isAppAdmin("deliciousfood", yibanBasicUserInfo.visit_user.userid))
			return new ErrorReporter(-1, "您不是管理员");
		VarietyOfDishes varietyOfDishes = varietyOfDishesDao.findOne(id);
		varietyOfDishesDao.delete(varietyOfDishes);
		return new JsonMes(1, "删除成功");
	}

	@RequestMapping("/test")
	public Object test() {
		int i;
		for (i = 0; i < 10; i++) {
			VarietyOfDishes varietyOfDishes = new VarietyOfDishes("宫爆鸡丁", "鲁菜", "酸辣", "荟萃2楼", "8.5",
					"./images/hongshao.jpg", "好吃");
			varietyOfDishesDao.save(varietyOfDishes);
		}
		return varietyOfDishesDao.findAll();
	}
}
