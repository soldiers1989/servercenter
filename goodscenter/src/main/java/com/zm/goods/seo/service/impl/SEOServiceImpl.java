package com.zm.goods.seo.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.zm.goods.bussiness.dao.GoodsMapper;
import com.zm.goods.bussiness.dao.GoodsTagMapper;
import com.zm.goods.bussiness.dao.SEOMapper;
import com.zm.goods.constants.Constants;
import com.zm.goods.enummodel.PublishType;
import com.zm.goods.enummodel.SystemEnum;
import com.zm.goods.feignclient.UserFeignClient;
import com.zm.goods.log.LogUtil;
import com.zm.goods.pojo.GoodsItem;
import com.zm.goods.pojo.GoodsSpecs;
import com.zm.goods.pojo.GoodsTagEntity;
import com.zm.goods.pojo.ResultModel;
import com.zm.goods.pojo.bo.ItemStockBO;
import com.zm.goods.pojo.po.PagePO;
import com.zm.goods.pojo.vo.GoodsIndustryModel;
import com.zm.goods.seo.model.GoodsTempModel;
import com.zm.goods.seo.model.SEOBase;
import com.zm.goods.seo.model.SEODetail;
import com.zm.goods.seo.model.SEOGoodsDel;
import com.zm.goods.seo.model.SEOModel;
import com.zm.goods.seo.model.SEONavigation;
import com.zm.goods.seo.publish.PublishComponent;
import com.zm.goods.seo.service.SEOService;
import com.zm.goods.utils.JSONUtil;

@Service("seoService")
public class SEOServiceImpl implements SEOService {

	@Resource
	SEOMapper seoMapper;

	@Resource
	GoodsMapper goodsMapper;

	@Resource
	RedisTemplate<String, Object> template;

	@Resource
	UserFeignClient userFeignClient;
	
	@Resource
	GoodsTagMapper goodsTagMapper;
	
	private static Integer CNCOOPBUY_ID = 2;
	private static String HTTP_STR = "http";

	@Override
	public List<ItemStockBO> getGoodsStock(String goodsId, Integer centerId) {
		List<String> itemIds = seoMapper.listItemIdsByGoodsId(goodsId);
		if (itemIds != null && itemIds.size() > 0) {
			return seoMapper.listStockByItemIds(itemIds);
		}
		return null;
	}

	@Override
	public ResultModel publish(List<String> itemIdList, Integer centerId, boolean isNewPublish) {
		List<String> goodsIdList = goodsMapper.getGoodsIdByItemId(itemIdList);
		if (goodsIdList == null || goodsIdList.size() == 0) {
			return new ResultModel(false, "没有对应的商品GOODSID");
		}
		return publishByGoodsId(goodsIdList, centerId, isNewPublish);
	}

	private boolean publishAndHandle(StringBuilder sb, ResultModel result, SEOBase base, PublishType publishType) {
		ResultModel temp = PublishComponent.publish(JSONUtil.toJson(base), publishType);
		if (!temp.isSuccess()) {
			result.setSuccess(false);
			if (base instanceof SEODetail) {
				SEODetail tem = (SEODetail) base;
				sb.append(tem.getFile() + ":");
			}
			sb.append(temp.getErrorMsg());
		}
		return temp.isSuccess();
	}

	private GoodsItem getGoods(String goodsId) {
		GoodsItem goodsItem = seoMapper.getGoods(goodsId);
		if (goodsItem == null) {
			return null;
		}
		if (goodsItem.getGoodsSpecsList() != null) {
			for (GoodsSpecs specs : goodsItem.getGoodsSpecsList()) {
				specs.infoFilter();
			}
		}
		//加标签
		addItemGoodsTag(goodsItem);
		
		HashOperations<String, String, String> hashOperations = template.opsForHash();
		Map<String, String> map = hashOperations.entries(Constants.POST_TAX + goodsItem.getSupplierId());
		if (map != null) {
			String post = map.get("post");
			String tax = map.get("tax");
			try {
				goodsItem.setFreePost(Integer.valueOf(post == null ? "0" : post));
				goodsItem.setFreeTax(Integer.valueOf(tax == null ? "0" : tax));
			} catch (Exception e) {
				LogUtil.writeErrorLog("【数字转换出错】" + post + "," + tax);
			}
		}
		return goodsItem;
	}

	@Override
	public ResultModel navPublish() {
		ResultModel result = new ResultModel(true, "");
		StringBuilder sb = new StringBuilder();
		List<GoodsIndustryModel> list = goodsMapper.queryGoodsCategory();
		SEONavigation seoNav = new SEONavigation("nav-1", SystemEnum.PCMALL, list);
		publishAndHandle(sb, result, seoNav, PublishType.MODULE_CREATE);
		seoNav = new SEONavigation("nav-1", SystemEnum.MPMALL, list);
		publishAndHandle(sb, result, seoNav, PublishType.MODULE_CREATE);
		if (sb.length() > 0) {
			sb.append("发布失败");
			result.setErrorMsg(sb.toString());
		}
		return result;
	}

	@Override
	public ResultModel delPublish(List<String> itemIdList, Integer centerId) {
		List<String> goodsIdList = goodsMapper.getGoodsIdByItemId(itemIdList);
		if (goodsIdList == null || goodsIdList.size() == 0) {
			return new ResultModel(false, "没有对应的商品GOODSID");
		}
		return delPublishByGoodsId(goodsIdList, centerId);
	}

	@Override
	public ResultModel indexPublish(Integer id) {

		// 检索数据
		PagePO pagePo = retrievePageData(id);

		// 数据转化为接口结构
		SEODetail seoDetail = convertToSEODetail(pagePo);

		// 发布并操作返回结构
		StringBuilder sb = new StringBuilder();
		ResultModel result = new ResultModel(true, "");
		publishAndHandle(sb, result, seoDetail, PublishType.PAGE_CREATE);
		if (sb.length() > 0) {
			sb.append("发布失败");
			result.setErrorMsg(sb.toString());
		} else {
			seoMapper.updatePagePublishToSave(pagePo);
			seoMapper.updatePageSaveToPublish(id);
		}
		return result;
	}

	/**
	 * convertToSEODetail:数据转化为接口结构. <br/>
	 * 
	 * @author hebin
	 * @param pagePo
	 * @return
	 * @since JDK 1.7
	 */
	private SEODetail convertToSEODetail(PagePO pagePo) {
		List<PagePO> pageList = new ArrayList<PagePO>();
		pageList.add(pagePo);
		SEODetail seoDetail = new SEODetail(null, pagePo.getsEOModel(), null, null,
				SystemEnum.getSystem(pagePo.getClient()), pageList);
		if (pagePo.getGradeId() != CNCOOPBUY_ID) {// 主站不需要
			String url = userFeignClient.getClientUrl(pagePo.getGradeId(), Constants.FIRST_VERSION);
			String region;
			if (url.startsWith(HTTP_STR)) {
				region = url.substring(url.indexOf("//") + 2, url.indexOf("."));
			} else {
				region = url.substring(0, url.indexOf("."));
			}
			seoDetail.setRegion(region);
		}
		return seoDetail;
	}

	/**
	 * retrievePageData:(这里用一句话描述这个方法的作用). <br/>
	 * 
	 * @author hebin
	 * @param id
	 * @return
	 * @since JDK 1.7
	 */
	private PagePO retrievePageData(Integer id) {
		PagePO page = seoMapper.getPageById(id);
		page.setsEOModel(seoMapper.getPageSEO(id));
		return page;
	}

	@Override
	public PagePO retrievePage(Integer id) throws Exception {
		PagePO page = seoMapper.getPageById(id);

		if (page == null) {
			throw new Exception("no page info!");
		}

		page.setsEOModel(seoMapper.getPageSEO(id));
		return page;
	}

	@Override
	public ResultModel getGoodsAccessPath(String goodsId, String itemId) {
		ResultModel result = new ResultModel();
		if (goodsId != null && !"".equals(goodsId)) {
			String path = seoMapper.getGoodsAccessPath(goodsId);
			result.setSuccess(true);
			result.setObj(path);
		} else if (itemId != null && !"".equals(itemId)) {
			goodsId = seoMapper.getGoodsIdByItemId(itemId);
			String path = seoMapper.getGoodsAccessPath(goodsId);
			result.setSuccess(true);
			result.setObj(path);
		} else {
			result.setSuccess(false);
			result.setErrorMsg("没有商品编号");
		}
		return result;
	}

	@Override
	public ResultModel publishByGoodsId(List<String> goodsIdList, Integer centerId, boolean isNewPublish) {

		// 检索手机端及商场的页面组建数据
		List<PagePO> pageList = seoMapper.getGoodsDetailPageByPublish();
		if (pageList == null || pageList.size() != 2) {
			return new ResultModel(false, "请确认手机和pc商详模板是否都存在");
		}

		// 数据转化
		StringBuilder sb = new StringBuilder();
		ResultModel result = new ResultModel(true, "");
		SEODetail seoGoodsDetail;
		boolean success;
		if (goodsIdList != null && goodsIdList.size() > 0) {
			Map<String, Object> param = new HashMap<String, Object>();
			for (String goodsId : goodsIdList) {
				GoodsItem goodsItem = getGoods(goodsId);
				if (goodsItem == null) {
					continue;
				}
				if (!isNewPublish) {// 如果不是新发布，是重新发布的，先把发布标志置为未发布
					seoMapper.updateGoodsRePublishByGoodsId(param);
				}
				String path = seoMapper.queryGoodsCategoryPath(goodsItem.getThirdCategory());
				template.opsForValue().set("href:" + goodsId, "/" + path + "/" + goodsId + ".html");
				param.put("accessPath", path);
				param.put("goodsId", goodsId);
				SEOModel seoModel = seoMapper.getGoodsSEO(goodsId);
				seoGoodsDetail = new SEODetail(goodsItem, seoModel, goodsId + ".html", path, SystemEnum.PCMALL,
						pageList);
				success = publishAndHandle(sb, result, seoGoodsDetail, PublishType.PAGE_CREATE);
				if (!success) {
					continue;
				}
				seoGoodsDetail = new SEODetail(goodsItem, seoModel, goodsId + ".html", path, SystemEnum.MPMALL,
						pageList);
				success = publishAndHandle(sb, result, seoGoodsDetail, PublishType.PAGE_CREATE);
				if (!success) {
					continue;
				}
				seoMapper.updateGoodsAccessPath(param);
				seoMapper.updateGoodsPublishByGoodsId(goodsId);
			}
			if (sb.length() > 0) {
				sb.append("发布失败");
				result.setErrorMsg(sb.toString());
			}
		}
		return result;
	}

	@Override
	public ResultModel delPublishByGoodsId(List<String> goodsIdList, Integer centerId) {
		ResultModel result = new ResultModel(true, "");
		List<GoodsTempModel> tempList = seoMapper.getDownShelvesGoodsIdByGoodsId(goodsIdList);
		if (tempList != null && tempList.size() > 0) {// 说明有商品所有规格都下架，需要删除已经发布的商详
			List<String> needDelgoodsIdList = new ArrayList<String>();
			List<String> needRePublishgoodsIdList = new ArrayList<String>();
			for (GoodsTempModel model : tempList) {
				if (model.getStatus() == 0) {
					needDelgoodsIdList.add(model.getGoodsId());// 需要删除的
				} else {
					needRePublishgoodsIdList.add(model.getGoodsId());// 需要重新发布的
				}
			}
			StringBuilder sb = new StringBuilder();
			boolean success;
			if (needDelgoodsIdList != null && needDelgoodsIdList.size() > 0) {
				SEOGoodsDel seoGoodsDel = null;
				for (String goodsId : needDelgoodsIdList) {
					String path = seoMapper.getGoodsAccessPath(goodsId);
					seoGoodsDel = new SEOGoodsDel(path, SystemEnum.PCMALL, goodsId + ".html");
					success = publishAndHandle(sb, result, seoGoodsDel, PublishType.PAGE_DELETE);
					if (!success) {
						continue;
					}
					seoGoodsDel = new SEOGoodsDel(path, SystemEnum.MPMALL, goodsId + ".html");
					success = publishAndHandle(sb, result, seoGoodsDel, PublishType.PAGE_DELETE);
					if (!success) {
						continue;
					}
					seoMapper.updateGoodsDelPublishByGoodsId(goodsId);
				}
			}
			if (sb.length() > 0) {
				sb.append("删除失败");
				result.setErrorMsg(sb.toString());
			}
			if (needRePublishgoodsIdList.size() > 0) {// 需要重新发布的goods
				ResultModel temp = publishByGoodsId(needRePublishgoodsIdList, centerId, false);
				result.setSuccess(temp.isSuccess());
				result.setErrorMsg(result.getErrorMsg() + temp.getErrorMsg());
			}
		}
		return result;
	}
	
	/**
	 * @fun 加标签
	 * @param goodsList
	 */
	private void addItemGoodsTag(GoodsItem goods) {
		List<String> itemIdList = new ArrayList<String>();
		if (goods != null) {
			if (goods.getGoodsSpecsList() != null) {
				for (GoodsSpecs specs : goods.getGoodsSpecsList()) {
					itemIdList.add(specs.getItemId());
				}
			}
			List<GoodsTagEntity> list = goodsTagMapper.listGoodsTagByGoodsId(itemIdList);
			List<GoodsTagEntity> temp = null;
			Map<String, List<GoodsTagEntity>> map = new HashMap<String, List<GoodsTagEntity>>();
			if (list != null && list.size() > 0) {
				for (GoodsTagEntity tag : list) {
					if (map.get(tag.getItemId()) == null) {
						temp = new ArrayList<GoodsTagEntity>();
						temp.add(tag);
						map.put(tag.getItemId(), temp);
					} else {
						map.get(tag.getItemId()).add(tag);
					}
				}
			}
			if (goods.getGoodsSpecsList() != null) {
				for (GoodsSpecs specs : goods.getGoodsSpecsList()) {
					specs.setTagList(map.get(specs.getItemId()));
				}
			}
		}
	}

}
