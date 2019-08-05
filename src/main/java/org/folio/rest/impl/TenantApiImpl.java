package org.folio.rest.impl;

import javax.ws.rs.core.Response;

import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.TenantAttributes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Map;

public class TenantApiImpl extends TenantAPI {

  private final Logger logger = LoggerFactory.getLogger(TenantApiImpl.class);

  public TenantApiImpl() {
    super();
  }

  @Validate
  @Override
  public void postTenant(TenantAttributes entity, Map<String, String> headers, Handler<AsyncResult<Response>> handlers,
                         Context context) {
    handlers.handle(Future.succeededFuture(TenantApiImpl.PostTenantResponse.respond204()));
  }
}
