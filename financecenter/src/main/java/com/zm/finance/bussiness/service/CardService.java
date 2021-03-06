package com.zm.finance.bussiness.service;

import com.github.pagehelper.Page;
import com.zm.finance.pojo.ResultModel;
import com.zm.finance.pojo.card.Card;

public interface CardService {

	ResultModel addCard(Card card);

	ResultModel modifyCard(Card card);

	ResultModel getCard(Integer gradeId);

	ResultModel removeCard(Integer id);

	ResultModel checkCardNo(String cardNo);

	Page<Card> getCardForPage(Card card);

	ResultModel queryByCardId(Card card);
}
