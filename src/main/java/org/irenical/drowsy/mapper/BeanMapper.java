package org.irenical.drowsy.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanMapper {

  private static final Logger LOG = LoggerFactory.getLogger(BeanMapper.class);

  public <OBJECT> List<OBJECT> map(ResultSet resultSet, Class<OBJECT> beanClass)
      throws SQLException, InstantiationException, IllegalAccessException {
    List<OBJECT> result = new LinkedList<>();
    if (resultSet.next()) {
      List<String> cols = new LinkedList<>();
      ResultSetMetaData md = resultSet.getMetaData();
      for (int c = 1; c <= md.getColumnCount(); ++c) {
        cols.add(md.getColumnName(c));
      }
      Map<String, Field> fields = new HashMap<>();
      for (Field field : beanClass.getDeclaredFields()) {
        field.setAccessible(true);
        fields.put(field.getName(), field);
      }
      do {
        OBJECT bean = beanClass.newInstance();
        rowToBean(bean, resultSet, cols, fields);
        result.add(bean);
      } while (resultSet.next());
    }
    return result;
  }

  private <OBJECT> void rowToBean(OBJECT bean, ResultSet set, List<String> cols,
      Map<String, Field> fields)
      throws SQLException, IllegalArgumentException, IllegalAccessException {
    for (String col : cols) {
      Object cell = set.getObject(col);
      if (cell == null) {
        continue;
      }
      Field field = fields.get(col);
      if (field == null) {
        LOG.warn("No field mapping for column " + col + " on class " + bean.getClass());
        continue;
      }
      Class<?> fieldType = getObjectType(field.getType());
      if (!fieldType.isAssignableFrom(cell.getClass())) {
        throw new SQLException("Field " + col + " on class " + bean.getClass() + " type mismatch. Expected "
            + cell.getClass());
      } else {
        field.set(bean, cell);
      }
    }
  }

  private Class<?> getObjectType(Class<?> type) {
    if(type.isPrimitive()){
      switch(type.getName()){
      case "boolean":
        return Boolean.class;
      case "byte":
        return Byte.class;
      case "char":
        return Character.class;
      case "short":
        return Short.class;
      case "int":
      return Integer.class;
      case "float":
        return Float.class;
      case "long":
        return Long.class;
      case "double":
        return Double.class;
        default:
          throw new java.lang.UnsupportedOperationException("Primitive type not supported: " + type.getName());
      }
    } else {
      return type;
    }
  }

}
