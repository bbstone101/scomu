package com.bbstone.comm.dto.req;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderReqDTO {

	private String id;
	
	private String symbol;
	
	/**
	 * 交易量
	 */
	private BigDecimal volume;
	/**
	 * 创建时间
	 */
	private long createDt;

	/**
	 * 修改时间
	 */
	private long modifyDt;
	
	/**
	 * 到期类型 1-GTC 2-今日 3-指定时间 4-指定天
	 */
	private Integer expirationType;

}
