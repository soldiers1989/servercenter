package com.zm.order.feignclient;

import java.io.IOException;
import java.util.Map;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.zm.order.feignclient.model.PayModel;
import com.zm.order.feignclient.model.RefundPayModel;

@FeignClient("paycenter")
public interface PayFeignClient {

	@RequestMapping(value = "wxpay/{type}/{clientId}", method = RequestMethod.POST)
	public Map<String, String> wxPay(@PathVariable("clientId") Integer clientId, @PathVariable("type") String type,
			@RequestBody PayModel model) throws Exception;

	@RequestMapping(value = "alipay/refund/{clientId}", method = RequestMethod.POST)
	public Map<String, Object> aliRefundPay(@PathVariable("clientId") Integer clientId,
			@RequestBody RefundPayModel model) throws Exception;

	@RequestMapping(value = "alipay/{type}/{clientId}", method = RequestMethod.POST)
	public String aliPay(@PathVariable("clientId") Integer clientId, @PathVariable("type") String type,
			@RequestBody PayModel model) throws Exception;

	@RequestMapping(value = "wx/refund/{clientId}", method = RequestMethod.POST)
	public Map<String, Object> wxRefundPay(@PathVariable("clientId") Integer clientId,
			@RequestBody RefundPayModel model) throws Exception;

	@RequestMapping(value = "unionpay/{type}/{clientId}", method = RequestMethod.POST)
	public Map<String, Object> unionpay(@PathVariable("clientId") Integer clientId, @PathVariable("type") String type,
			@RequestBody PayModel model);

	@RequestMapping(value = "yoppay/{clientId}", method = RequestMethod.POST)
	public Map<String, Object> yopPay(@PathVariable("clientId") Integer clientId, @RequestBody PayModel model)
			throws IOException;
}
