package com.cyan.controller;

import java.math.BigDecimal;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cyan.domain.Product;

@RestController
public class CommerceController {

	@RequestMapping(value = "/top_seller", produces = MediaType.APPLICATION_JSON_VALUE,  method = RequestMethod.GET)
	public Product getTopSeller() {
		Product topSeller = new Product();
		topSeller.setName("Great Wall");
		topSeller.setPrice(new BigDecimal(10));
		
		return topSeller;
	}
}
