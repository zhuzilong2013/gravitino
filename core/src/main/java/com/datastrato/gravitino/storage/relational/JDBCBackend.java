/*
 * Copyright 2024 Datastrato Pvt Ltd.
 * This software is licensed under the Apache License version 2.
 */

package com.datastrato.gravitino.storage.relational;

import com.datastrato.gravitino.Config;
import com.datastrato.gravitino.Configs;
import com.datastrato.gravitino.Entity;
import com.datastrato.gravitino.EntityAlreadyExistsException;
import com.datastrato.gravitino.HasIdentifier;
import com.datastrato.gravitino.NameIdentifier;
import com.datastrato.gravitino.Namespace;
import com.datastrato.gravitino.UnsupportedEntityTypeException;
import com.datastrato.gravitino.exceptions.AlreadyExistsException;
import com.datastrato.gravitino.exceptions.NoSuchEntityException;
import com.datastrato.gravitino.meta.BaseMetalake;
import com.datastrato.gravitino.storage.relational.service.MetalakeMetaService;
import com.datastrato.gravitino.storage.relational.session.SqlSessionFactoryHelper;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * {@link JDBCBackend} is a jdbc implementation of {@link RelationalBackend} interface. You can use
 * a database that supports the JDBC protocol as storage. If the specified database has special SQL
 * syntax, please implement the SQL statements and methods in MyBatis Mapper separately and switch
 * according to the {@link Configs#ENTITY_RELATIONAL_JDBC_BACKEND_URL_KEY} parameter.
 */
public class JDBCBackend implements RelationalBackend {

  /** Initialize the jdbc backend instance. */
  @Override
  public void initialize(Config config) {
    SqlSessionFactoryHelper.getInstance().init(config);
  }

  @Override
  public <E extends Entity & HasIdentifier> List<E> list(
      Namespace namespace, Entity.EntityType entityType) {
    switch (entityType) {
      case METALAKE:
        return (List<E>) MetalakeMetaService.getInstance().listMetalakes();
      default:
        throw new UnsupportedEntityTypeException(
            "Unsupported entity type: %s for list operation", entityType);
    }
  }

  @Override
  public boolean exists(NameIdentifier ident, Entity.EntityType entityType) {
    try {
      Entity entity = get(ident, entityType);
      return entity != null;
    } catch (NoSuchEntityException ne) {
      return false;
    }
  }

  @Override
  public <E extends Entity & HasIdentifier> void insert(E e, boolean overwritten)
      throws EntityAlreadyExistsException {
    if (e instanceof BaseMetalake) {
      MetalakeMetaService.getInstance().insertMetalake((BaseMetalake) e, overwritten);
    } else {
      throw new UnsupportedEntityTypeException(
          "Unsupported entity type: %s for insert operation", e.getClass());
    }
  }

  @Override
  public <E extends Entity & HasIdentifier> E update(
      NameIdentifier ident, Entity.EntityType entityType, Function<E, E> updater)
      throws IOException, NoSuchEntityException, AlreadyExistsException {
    switch (entityType) {
      case METALAKE:
        return (E) MetalakeMetaService.getInstance().updateMetalake(ident, updater);
      default:
        throw new UnsupportedEntityTypeException(
            "Unsupported entity type: %s for update operation", entityType);
    }
  }

  @Override
  public <E extends Entity & HasIdentifier> E get(
      NameIdentifier ident, Entity.EntityType entityType) throws NoSuchEntityException {
    switch (entityType) {
      case METALAKE:
        return (E) MetalakeMetaService.getInstance().getMetalakeByIdent(ident);
      default:
        throw new UnsupportedEntityTypeException(
            "Unsupported entity type: %s for get operation", entityType);
    }
  }

  @Override
  public boolean delete(NameIdentifier ident, Entity.EntityType entityType, boolean cascade) {
    switch (entityType) {
      case METALAKE:
        return MetalakeMetaService.getInstance().deleteMetalake(ident, cascade);
      default:
        throw new UnsupportedEntityTypeException(
            "Unsupported entity type: %s for delete operation", entityType);
    }
  }

  @Override
  public void close() throws IOException {
    SqlSessionFactoryHelper.getInstance().close();
  }
}
