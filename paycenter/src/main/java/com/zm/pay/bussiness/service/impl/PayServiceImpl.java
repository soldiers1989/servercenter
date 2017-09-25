package com.zm.pay.bussiness.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zm.pay.bussiness.service.PayService;
import com.zm.pay.constants.Constants;
import com.zm.pay.constants.LogConstants;
import com.zm.pay.feignclient.LogFeignClient;
import com.zm.pay.pojo.PayModel;
import com.zm.pay.pojo.WeixinPayConfig;
import com.zm.pay.utils.CommonUtils;
import com.zm.pay.utils.wx.WxPayUtils;

/**
 * ClassName: PayServiceImpl <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * date: Aug 11, 2017 3:45:10 PM <br/>
 * 
 * @author wqy
 * @version
 * @since JDK 1.7
 */
@Service
public class PayServiceImpl implements PayService{

	@Resource
	LogFeignClient logFeignClient;
	
	@Resource
	RedisTemplate<String, ?> redisTemplate;
	
	
	@Override
	public Map<String, String> weiXinPay(Integer clientId, String type, PayModel model) throws Exception {
		WeixinPayConfig config = (WeixinPayConfig) redisTemplate.opsForValue()
				.get(Constants.PAY + clientId + Constants.WX_PAY);

		config.setHttpConnectTimeoutMs(5000);
		config.setHttpReadTimeoutMs(5000);

		Map<String, String> resp = WxPayUtils.unifiedOrder(type, config, model);

		String return_code = (String) resp.get("return_code");
		String result_code = (String) resp.get("result_code");
		Map<String, String> result = new HashMap<String, String>();

		if ("SUCCESS".equals(return_code) && "SUCCESS".equals(result_code)) {
			String content = "订单号 \"" + model.getOrderId() + "\" 通过微信支付" + type + "，后台请求成功";
			logFeignClient.saveLog(Constants.FIRST_VERSION,
					CommonUtils.packageLog(LogConstants.WX_PAY, "微信支付", clientId, content, ""));

			WxPayUtils.packageReturnParameter(clientId, type, model, config, resp, result);
		} else {
			result.put("success", "false");
			if("SUCCESS".equals(return_code)){
				result.put("errorMsg", (String) resp.get("err_code_des"));
			}
		}
		return result;
	}

}