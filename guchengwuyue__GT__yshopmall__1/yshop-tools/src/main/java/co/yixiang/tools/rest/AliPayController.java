/**
* Copyright (C) 2018-2020
* All rights reserved, Designed By www.yixiang.co
* 注意：
* 本软件为www.yixiang.co开发研制
*/
package co.yixiang.tools.rest;

import co.yixiang.annotation.AnonymousAccess;
import co.yixiang.logging.aop.log.Log;
import co.yixiang.tools.domain.AlipayConfig;
import co.yixiang.tools.domain.vo.TradeVo;
import co.yixiang.tools.service.AlipayConfigService;
import co.yixiang.tools.utils.AliPayStatusEnum;
import co.yixiang.tools.utils.AlipayUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author hupeng
 * @date 2018-12-31
 */
@Slf4j
@RestController
@RequestMapping("/api/aliPay")
@Api(tags = "工具：支付宝管理")
public class AliPayController {

	private final AlipayUtils alipayUtils;

	private final AlipayConfigService alipayService;

	public AliPayController(AlipayUtils alipayUtils, AlipayConfigService alipayService) {
		this.alipayUtils = alipayUtils;
		this.alipayService = alipayService;
	}

	@GetMapping
	public ResponseEntity<AlipayConfig> get() {
		return new ResponseEntity<>(alipayService.find(), HttpStatus.OK);
	}

	@Log("配置支付宝")
	@ApiOperation("配置支付宝")
	@PutMapping
	public ResponseEntity<Object> payConfig(@Validated @RequestBody AlipayConfig alipayConfig) {
		alipayConfig.setId(1L);
		alipayService.update(alipayConfig);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Log("支付宝PC网页支付")
	@ApiOperation("PC网页支付")
	@PostMapping(value = "/toPayAsPC")
	public ResponseEntity<String> toPayAsPc(@Validated @RequestBody TradeVo trade) throws Exception {
		AlipayConfig aliPay = alipayService.find();
		trade.setOutTradeNo(alipayUtils.getOrderCode());
		String payUrl = alipayService.toPayAsPc(aliPay, trade);
		return ResponseEntity.ok(payUrl);
	}

	@Log("支付宝手机网页支付")
	@ApiOperation("手机网页支付")
	@PostMapping(value = "/toPayAsWeb")
	public ResponseEntity<String> toPayAsWeb(@Validated @RequestBody TradeVo trade) throws Exception {
		AlipayConfig alipay = alipayService.find();
		trade.setOutTradeNo(alipayUtils.getOrderCode());
		String payUrl = alipayService.toPayAsWeb(alipay, trade);
		return ResponseEntity.ok(payUrl);
	}

	@ApiIgnore
	@GetMapping("/return")
	@AnonymousAccess
	@ApiOperation("支付之后跳转的链接")
	public ResponseEntity<String> returnPage(HttpServletRequest request, HttpServletResponse response) {
		AlipayConfig alipay = alipayService.find();
		response.setContentType("text/html;charset=" + alipay.getCharset());
		//内容验签，防止黑客篡改参数
		if (alipayUtils.rsaCheck(request, alipay)) {
			//商户订单号
			String outTradeNo = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
			//支付宝交易号
			String tradeNo = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
			System.out.println("商户订单号" + outTradeNo + "  " + "第三方交易号" + tradeNo);

			// 根据业务需要返回数据，这里统一返回OK
			return new ResponseEntity<>("payment successful", HttpStatus.OK);
		} else {
			// 根据业务需要返回数据
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@ApiIgnore
	@RequestMapping("/notify")
	@AnonymousAccess
	@SuppressWarnings("all")
	@ApiOperation("支付异步通知(要公网访问)，接收异步通知，检查通知内容app_id、out_trade_no、total_amount是否与请求中的一致，根据trade_status进行后续业务处理")
	public ResponseEntity<Object> notify(HttpServletRequest request) {
		AlipayConfig alipay = alipayService.find();
		Map<String, String[]> parameterMap = request.getParameterMap();
		//内容验签，防止黑客篡改参数
		if (alipayUtils.rsaCheck(request, alipay)) {
			//交易状态
			String tradeStatus = new String(request.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
			// 商户订单号
			String outTradeNo = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
			//支付宝交易号
			String tradeNo = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
			//付款金额
			String totalAmount = new String(request.getParameter("total_amount").getBytes(StandardCharsets.ISO_8859_1),
					StandardCharsets.UTF_8);
			//验证
			if (tradeStatus.equals(AliPayStatusEnum.SUCCESS.getValue())
					|| tradeStatus.equals(AliPayStatusEnum.FINISHED.getValue())) {
				// 验证通过后应该根据业务需要处理订单
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
}
