package io.jboot.test.aop.cache;

import io.jboot.aop.annotation.Bean;
import io.jboot.components.cache.annotation.CacheEvict;
import io.jboot.components.cache.annotation.CachePut;
import io.jboot.components.cache.annotation.Cacheable;
import io.jboot.test.db.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Bean
public class CommentServiceImpl implements CommentService {

	@Override
	public String getCommentById(String id) {
		return "id:" + id + "  data:" + UUID.randomUUID();
	}

	@Override
	@Cacheable(name = "cacheName", key = "#(id)")
	public String getCommentByIdWithCache(String id) {
		return "id:" + id + "  data:" + UUID.randomUUID();
	}

	@Override
	@Cacheable(name = "cacheName", key = "#(id)", liveSeconds = 5)
	public String getCommentByIdWithCacheTime(String id) {
		return "id:" + id + "  data:" + UUID.randomUUID();
	}

	@Override
	@CachePut(name = "cacheName", key = "#(id)")
	public String updateCache(String id) {
		return "id:" + id + "  data:" + UUID.randomUUID();
	}

	@Override
	@CacheEvict(name = "cacheName", key = "#(id)")
	public void delCache(String id) {
	}

	@Override
	@Cacheable(name = "cacheName", returnCopyEnable = true)
	public List<User> findList() {
		List<User> userList = new ArrayList<>();
		userList.add(new User());
		return userList;
	}

	@Override
	@Cacheable(name = "cacheName", returnCopyEnable = true)
	public User[] findArray() {
		User[] users = new User[] { new User() };
		return users;
	}
}
