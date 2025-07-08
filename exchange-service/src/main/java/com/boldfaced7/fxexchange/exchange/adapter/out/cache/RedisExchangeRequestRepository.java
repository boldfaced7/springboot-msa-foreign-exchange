package com.boldfaced7.fxexchange.exchange.adapter.out.cache;

import org.springframework.data.repository.CrudRepository;

public interface RedisExchangeRequestRepository extends CrudRepository<RedisExchangeRequest, String> {
}
