package com.zm.finance.bussiness.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.zm.finance.pojo.RebateDownload;
import com.zm.finance.pojo.RebateSearchModel;
import com.zm.finance.pojo.ResultModel;
import com.zm.finance.pojo.rebate.Rebate;
import com.zm.finance.pojo.rebate.Rebate4Order;
import com.zm.finance.pojo.rebate.RebateDetail;

public interface RebateService {

	/**
	 * @fun 定时将redis里的返佣记录保存到数据库
	 */
	void updateRebateTask();

	/**
	 * @fun 获取返佣
	 * @param gradeId
	 * @return
	 */
	ResultModel getRebate(Integer gradeId);

	Page<RebateDetail> getRebateDetail(RebateDetail entity);

	Page<Rebate> listRebate(RebateSearchModel search);

	void saveRebateDetail(Map<String, String> map);

	void updateRebateDetail(String orderId, Integer status);

	void redisTool(String key, Map<String, String> map);

	void redisToolList(String key, String value);

	List<RebateDownload> listRebateDetailForDownload(Map<String,Object> param);

	ResultModel saveRebate4order(Rebate4Order rebate4Order);

	void rebate4orderBatch(List<Rebate4Order> rebate4OrderList);

}
