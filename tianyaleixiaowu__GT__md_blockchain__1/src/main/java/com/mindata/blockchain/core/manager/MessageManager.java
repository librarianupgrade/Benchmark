package com.mindata.blockchain.core.manager;

import com.mindata.blockchain.core.model.MessageEntity;
import com.mindata.blockchain.core.repository.MessageRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuweifeng wrote on 2018/3/28.
 */
@Component
public class MessageManager {
	@Resource
	private MessageRepository messageRepository;

	public List<MessageEntity> findAll() {
		return messageRepository.findAll();
	}
}
