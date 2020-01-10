package com.microce.stacks.starter.action;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.microce.plugin.response.R;

@RestController
@RequestMapping("/simple")
public class SimpleRestful {

	// @RequestMapping(value = "japannet/cancel" , method = RequestMethod.POST,
	// consumes = "application/x-www-form-urlencoded;charset=SHIFT_JIS")

	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	public R<?> get(@RequestParam("id") String id) {
		return R.ok(id);
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public R<?> create(@RequestParam("id") String id, @RequestBody String body) {
		return R.ok(body);
	}

}
