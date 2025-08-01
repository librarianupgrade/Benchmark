package cn.imhtb.antlive.service;

import cn.imhtb.antlive.common.ApiResponse;
import cn.imhtb.antlive.entity.database.UserRole;
import cn.imhtb.antlive.vo.request.UserRoleUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author PinTeh
 * @date 2020/5/9
 */
public interface IUserRoleService extends IService<UserRole> {

	ApiResponse updateUserRole(UserRoleUpdateRequest request);

	List<Integer> listHasRoleUserIds();
}
