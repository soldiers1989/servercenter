/**  
 * Project Name:goodscenter  
 * File Name:BrandService.java  
 * Package Name:com.zm.goods.bussiness.service.impl  
 * Date:Nov 9, 20178:37:14 PM  
 *  
 */
package com.zm.goods.bussiness.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zm.goods.bussiness.dao.GoodsBackMapper;
import com.zm.goods.bussiness.dao.GoodsItemMapper;
import com.zm.goods.bussiness.service.GoodsBackService;
import com.zm.goods.constants.Constants;
import com.zm.goods.pojo.ERPGoodsTagBindEntity;
import com.zm.goods.pojo.ERPGoodsTagEntity;
import com.zm.goods.pojo.GoodsEntity;
import com.zm.goods.pojo.GoodsFile;
import com.zm.goods.pojo.GoodsRebateEntity;
import com.zm.goods.pojo.TagFuncEntity;
import com.zm.goods.pojo.ThirdWarehouseGoods;

/**
 * ClassName: GoodsBackServiceImpl <br/>
 * Function: 品牌服务. <br/>
 * date: Nov 9, 2017 8:37:14 PM <br/>
 * 
 * @author hebin
 * @version
 * @since JDK 1.7
 */
@Service
public class GoodsBackServiceImpl implements GoodsBackService {

	@Resource
	GoodsBackMapper goodsBackMapper;

	@Resource
	GoodsItemMapper goodsItemMapper;

	@Resource
	RedisTemplate<String, Object> template;

	@Override
	public Page<GoodsEntity> queryByPage(GoodsEntity entity) {
		PageHelper.startPage(entity.getCurrentPage(), entity.getNumPerPage(), true);
		return goodsBackMapper.selectForPage(entity);
	}

	@Override
	public GoodsEntity queryById(int id) {
		GoodsEntity entity = goodsBackMapper.selectById(id);
		List<GoodsFile> fileList = goodsBackMapper.selectGoodsFileByGoodsId(entity);
		GoodsEntity entityWithItem = goodsBackMapper.selectGoodsWithItem(id);
		ERPGoodsTagBindEntity tagBind = goodsBackMapper.selectGoodsTagBindByGoodsId(entityWithItem.getGoodsItem());
		entity.setFiles(fileList);
		entity.setGoodsTagBind(tagBind);
		return entity;
	}

	@Override
	@Transactional(isolation=Isolation.READ_COMMITTED)
	public void save(GoodsEntity entity, String type) {
		goodsBackMapper.insert(entity);
		goodsItemMapper.insert(entity.getGoodsItem());
		goodsItemMapper.insertPrice(entity.getGoodsItem().getGoodsPrice());
		if (entity.getFiles() != null && entity.getFiles().size() > 0) {
			goodsItemMapper.insertFiles(entity.getFiles());
		}

		if ("sync".equals(type)) {
			goodsBackMapper.updateThirdStatus(entity.getThirdId());
		}
		if (entity.getGoodsTagBind() != null) {
			goodsBackMapper.insertTagBind(entity.getGoodsTagBind());
		}
	}

	@Override
	public Page<ThirdWarehouseGoods> queryByPage(ThirdWarehouseGoods entity) {
		PageHelper.startPage(entity.getCurrentPage(), entity.getNumPerPage(), true);
		return goodsBackMapper.selectThirdForPage(entity);
	}

	@Override
	public ThirdWarehouseGoods queryThird(ThirdWarehouseGoods entity) {
		return goodsBackMapper.selectThird(entity);
	}

	@Override
	@Transactional(isolation=Isolation.READ_COMMITTED)
	public void edit(GoodsEntity entity) {
		goodsBackMapper.update(entity);
		List<GoodsFile> isrFileList = new ArrayList<GoodsFile>();
		if (entity.getFiles() != null && entity.getFiles().size() > 0) {
			for (GoodsFile gf : entity.getFiles()) {
				if (gf.getId() != null) {
					goodsItemMapper.updateFiles(gf);
				} else {
					isrFileList.add(gf);
				}
			}
		}
		if (isrFileList != null && isrFileList.size() > 0) {
			goodsItemMapper.insertFiles(isrFileList);
		}
		//判断商品标签
//		if (entity.getGoodsTagBind() != null) {
//			//增删改
//			ERPGoodsTagBindEntity oldTag = goodsBackMapper.selectGoodsTagBindByGoodsId(entity.getGoodsItem());
//			ERPGoodsTagBindEntity newTag = entity.getGoodsTagBind();
//			if (oldTag == null && newTag.getTagId() != 0) {
//				goodsBackMapper.insertTagBind(newTag);
//			} else if (oldTag != null && newTag.getTagId() == 0) {
//				goodsBackMapper.deleteTagBind(newTag);
//			} else if (oldTag != null && newTag.getTagId() != 0) {
//				goodsBackMapper.updateTagBind(newTag);
//			}
//		}
	}

	@Override
	@Transactional(isolation=Isolation.READ_COMMITTED)
	public void remove(GoodsEntity entity) {
		goodsBackMapper.delete(entity);
		goodsItemMapper.delete(entity);
	}

	@Override
	public GoodsEntity checkRecordForDel(GoodsEntity entity) {
		GoodsEntity retEntity = goodsBackMapper.selectRecordForDel(entity);
		return retEntity;
	}

	@Override
	public void saveDetailPath(GoodsEntity entity) {
		goodsBackMapper.updateDetailPath(entity);
	}

	@Override
	public GoodsEntity checkRecordForUpd(GoodsEntity entity) {
		GoodsEntity retEntity = goodsBackMapper.selectRecordForUpd(entity);
		return retEntity;
	}

	@Override
	public Page<GoodsRebateEntity> queryAllGoods(GoodsEntity entity) {
		PageHelper.startPage(entity.getCurrentPage(), entity.getNumPerPage(), true);
		return goodsBackMapper.selectAllGoodsForRebate(entity);
	}

	@Override
	public GoodsRebateEntity queryById(GoodsRebateEntity entity) {
		return goodsBackMapper.selectGoodsRebateById(entity);
	}

	@Override
	public GoodsRebateEntity checkRecordForRebate(GoodsRebateEntity entity) {
		return goodsBackMapper.selectRecordForRebate(entity);
	}

	@Override
	public void insertGoodsRebate(GoodsRebateEntity entity) {
		goodsBackMapper.insertGoodsRebate(entity);
		setRedis(entity);
	}

	private void setRedis(GoodsRebateEntity entity) {
		HashOperations<String, String, String> hashOperations = template.opsForHash();
		Map<String, String> result = new HashMap<String, String>();
		result.put("first", entity.getFirst());
		result.put("second", entity.getSecond());
		result.put("third", entity.getThird());
		hashOperations.putAll(Constants.GOODS_REBATE + entity.getGoodsId(), result);
	}


	@Override
	public void updateGoodsRebate(GoodsRebateEntity entity) {
		goodsBackMapper.updateGoodsRebate(entity);
		setRedis(entity);
	}

	@Override
	public Page<ERPGoodsTagEntity> queryTagForPage(ERPGoodsTagEntity entity) {
		PageHelper.startPage(entity.getCurrentPage(), entity.getNumPerPage(), true);
		return goodsBackMapper.selectTagForPage(entity);
	}

	@Override
	public void insertGoodsTag(ERPGoodsTagEntity entity) {
		goodsBackMapper.insertGoodsTag(entity);
	}
	
	@Override
	public void updateGoodsTag(ERPGoodsTagEntity entity) {
		goodsBackMapper.updateGoodsTag(entity);
	}
	
	@Override
	public void deleteGoodsTag(ERPGoodsTagEntity entity) {
		goodsBackMapper.deleteGoodsTag(entity);
	}

	@Override
	public ERPGoodsTagEntity queryTagInfo(ERPGoodsTagEntity entity) {
		return goodsBackMapper.selectTagInfo(entity);
	}

	@Override
	public List<ERPGoodsTagEntity> queryTagListInfo() {
		return goodsBackMapper.selectTagListInfo();
	}

	@Override
	public List<TagFuncEntity> queryTagFuncList() {
		return goodsBackMapper.selectTagFuncListInfo();
	}
}
