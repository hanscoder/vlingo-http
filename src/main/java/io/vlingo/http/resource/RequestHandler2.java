/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;

public class RequestHandler2<T, R> extends RequestHandler {
  final private ParameterResolver<T> resolverParam1;
  final private ParameterResolver<R> resolverParam2;
  private Handler2<T, R> handler;

  RequestHandler2(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2));
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
  }

  @FunctionalInterface
  public interface Handler2<T, R> {
    Response execute(T param1, R param2);
  }

  public RequestHandler2<T, R> handle(final Handler2<T, R> handler) {
    this.handler = handler;
    return this;
  }

  Response execute(final T param1, final R param2) {
    if (handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return handler.execute(param1, param2);
  }

  @Override
  public Response execute(Request request, Action.MappedParameters mappedParameters) {
    final T param1 = resolverParam1.apply(request, mappedParameters);
    final R param2 = resolverParam2.apply(request, mappedParameters);
    return this.execute(param1, param2);
  }

  // region FluentAPI
  public <U> RequestHandler3<T, R, U> param(final Class<U> paramClass) {
    return new RequestHandler3<>(method, path, resolverParam1, resolverParam2, ParameterResolver.path(2, paramClass));
  }

  public <U> RequestHandler3<T, R, U> body(final Class<U> bodyClass) {
    return new RequestHandler3<>(method, path, resolverParam1, resolverParam2, ParameterResolver.body(bodyClass));
  }

  public RequestHandler3<T, R, String> query(final String name) {
    return query(name, String.class);
  }

  public <U> RequestHandler3<T, R, U> query(final String name, final Class<U> queryClass) {
    return new RequestHandler3<>(method, path, resolverParam1, resolverParam2, ParameterResolver.query(name, queryClass));
  }

  public RequestHandler3<T, R, Header> header(final String name) {
    return new RequestHandler3<>(method, path, resolverParam1, resolverParam2, ParameterResolver.header(name));
  }
  // endregion
}
