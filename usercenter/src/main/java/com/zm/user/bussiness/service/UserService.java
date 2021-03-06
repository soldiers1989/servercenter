package com.zm.user.bussiness.service;

import java.util.List;
import java.util.Map;

import com.zm.user.common.ResultModel;
import com.zm.user.pojo.AbstractPayConfig;
import com.zm.user.pojo.Address;
import com.zm.user.pojo.Grade;
import com.zm.user.pojo.ThirdLogin;
import com.zm.user.pojo.UserDetail;
import com.zm.user.pojo.UserInfo;
import com.zm.user.pojo.UserVip;
import com.zm.user.pojo.VipOrder;
import com.zm.user.pojo.VipPrice;

/**
 * ClassName: UserService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * date: Aug 16, 2017 4:04:48 PM <br/>
 * 
 * @author wqy
 * @version
 * @since JDK 1.7
 */
public interface UserService {

	/**
	 * userNameVerify:验证用户名是否被注册. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @return
	 * @since JDK 1.7
	 */
	boolean userNameVerify(Map<String, String> param);

	/**
	 * getUserInfo:获取用户信息. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @since JDK 1.7
	 */
	UserInfo getUserInfo(Map<String, Object> param);

	/**
	 * saveAddress:保存收货地址. <br/>
	 * 
	 * @author wqy
	 * @param address
	 * @since JDK 1.7
	 */
	void saveAddress(Address address);

	/**
	 * listAddress:获取收货地址. <br/>
	 * 
	 * @author wqy
	 * @param userId
	 * @since JDK 1.7
	 */
	List<Address> listAddress(Integer userId);

	/**
	 * updateAddress:修改收货地址. <br/>
	 * 
	 * @author wqy
	 * @param address
	 * @return
	 * @since JDK 1.7
	 */
	ResultModel updateAddress(Address address);

	/**
	 * removeAddress:删除收货地址. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @since JDK 1.7
	 */
	void removeAddress(Map<String, Object> param);

	/**
	 * saveUserDetail:更新用户个人资料. <br/>
	 * 
	 * @author wqy
	 * @param detail
	 * @since JDK 1.7
	 */
	void updateUserDetail(UserDetail detail);

	/**
	 * saveUser:注册保存用户. <br/>
	 * 
	 * @author wqy
	 * @param info
	 * @since JDK 1.7
	 */
	Integer saveUser(UserInfo info);

	/**
	 * saveUserDetail:保存用户详细信息. <br/>
	 * 
	 * @author wqy
	 * @param info
	 * @since JDK 1.7
	 */
	void saveUserDetail(UserDetail info);

	/**
	 * modifyPwd:修改密码. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @since JDK 1.7
	 */
	void modifyPwd(Map<String, Object> param, String phone);

	/**
	 * getVipUser:获取用户信息,包含是否会员. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @since JDK 1.7
	 */
	UserInfo getVipUser(Integer userId, Integer centerId);

	/**
	 * saveVipOrder:保存订单并且调用支付. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @since JDK 1.7
	 */
	ResultModel saveVipOrder(VipOrder order, String payType, String type, AbstractPayConfig payConfig) throws Exception;

	/**
	 * listVipPrice:获取会员价格信息. <br/>
	 * 
	 * @author wqy
	 * @param param
	 * @since JDK 1.7
	 */
	List<VipPrice> listVipPrice(Integer centerId);

	UserVip getVipUserByOrderId(String orderId);

	void saveUserVip(UserVip userVip);

	void updateUserVip(UserVip userVip);

	void updateVipOrder(String orderId);

	/**
	 * isAlreadyPay:支付回调时判断该订单是否已经支付处理完成. <br/>
	 * 
	 * @author wqy
	 * @param orderId
	 * @since JDK 1.7
	 */
	boolean isAlreadyPay(String orderId);

	/**
	 * verifyIsFirst:第三方登录时查询是否第一次. <br/>
	 * 
	 * @author wqy
	 * @param info
	 * @since JDK 1.7
	 */
	boolean verifyIsFirst(ThirdLogin info);

	/**
	 * saveGrade:新增区域中心. <br/>
	 * 
	 * @author wqy
	 * @param grade
	 * @since JDK 1.7
	 */
	ResultModel saveGrade(Grade grade);

	/**
	 * getCenterId:获取所有区域中心ID. <br/>
	 * 
	 * @author wqy
	 * @param 
	 * @since JDK 1.7
	 */
	List<Integer> getCenterId();

	/**
	 * getUserIdentityId:获取用户身份证信息. <br/>
	 * 
	 * @author wqy
	 * @param 
	 * @since JDK 1.7
	 */
	UserInfo getUserIdentityId(Integer userId);

	List<Grade> listGradeByParentId(Integer parentId);

	String getPhoneByUserId(Integer userId);

	ResultModel getAllCustomer();

	ResultModel userBindInviterCode(UserInfo info);

	ResultModel userCheckInviterInfo(UserInfo info);
	
	ResultModel getAllUserInfoForShopByParam(boolean needPaging, UserInfo entity);
}
