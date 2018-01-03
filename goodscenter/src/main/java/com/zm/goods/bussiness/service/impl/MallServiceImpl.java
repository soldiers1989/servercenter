/**  
 * Project Name:goodscenter  
 * File Name:MallServiceImpl.java  
 * Package Name:com.zm.goods.bussiness.service.impl  
 * Date:Jan 2, 20182:57:31 PM  
 *  
 */
package com.zm.goods.bussiness.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zm.goods.bussiness.dao.MallMapper;
import com.zm.goods.bussiness.service.MallService;
import com.zm.goods.pojo.DictData;
import com.zm.goods.pojo.Layout;
import com.zm.goods.pojo.PopularizeDict;

/**
 * ClassName: MallServiceImpl <br/>
 * date: Jan 2, 2018 2:57:31 PM <br/>
 * 
 * @author hebin
 * @version
 * @since JDK 1.7
 */
@Service
public class MallServiceImpl implements MallService {

	@Resource
	MallMapper mallMapper;

	private final int PC_AD_NUM = 4;

	@Override
	public Page<PopularizeDict> queryDictByPage(PopularizeDict entity) {
		PageHelper.startPage(entity.getCurrentPage(), entity.getNumPerPage(), true);
		return mallMapper.selectDictForPage(entity);
	}

	@Override
	public PopularizeDict queryDictById(PopularizeDict dict) {
		return mallMapper.selectDictById(dict);
	}

	@Override
	@Transactional
	public void saveDict(PopularizeDict entity) {
		entity.getLayout().setCenterId(entity.getCenterId());
		mallMapper.insertLayout(entity.getLayout());
		entity.setLayoutId(entity.getLayout().getId());
		mallMapper.insertDict(entity);
	}

	@Override
	public Page<DictData> queryDataByPage(DictData entity) {
		PageHelper.startPage(entity.getCurrentPage(), entity.getNumPerPage(), true);
		return mallMapper.selectDataForPage(entity);
	}

	@Override
	public void saveData(DictData entity) {
		mallMapper.insertData(entity);
	}

	@Override
	public DictData queryDataById(DictData entity) {
		return mallMapper.selectDataById(entity);
	}

	@Override
	public List<DictData> queryDataAll(Layout layout) {
		return mallMapper.selectDataAll(layout);
	}

	@Override
	@Transactional
	public void initData(PopularizeDict entity) {
		mallMapper.insertLayout(entity.getLayout());
		entity.setLayoutId(entity.getLayout().getId());
		mallMapper.insertDict(entity);

		if ("module_00006".equals(entity.getLayout().getCode())) {
			List<DictData> dataList = new ArrayList<DictData>();
			for (int i = 0; i < PC_AD_NUM; i++) {
				DictData data = new DictData();
				data.setDictId(entity.getId());
				dataList.add(data);
			}
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("centerId",entity.getCenterId());
			map.put("list", dataList);
			mallMapper.insertDataBatch(map);
		}
	}

	@Override
	public void updateData(DictData entity) {
		mallMapper.updateData(entity);
	}
}
