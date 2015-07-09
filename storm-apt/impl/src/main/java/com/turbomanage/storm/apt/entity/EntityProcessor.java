/*******************************************************************************
 * Copyright 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.turbomanage.storm.apt.entity;

import com.turbomanage.storm.SQLiteDao;
import com.turbomanage.storm.api.Entity;
import com.turbomanage.storm.api.Id;
import com.turbomanage.storm.apt.BaseDaoModel;
import com.turbomanage.storm.apt.ClassProcessor;
import com.turbomanage.storm.apt.SqlUtil;
import com.turbomanage.storm.apt.StormEnvironment;
import com.turbomanage.storm.apt.converter.ConverterModel;
import com.turbomanage.storm.apt.database.DatabaseModel;
import com.turbomanage.storm.exception.TypeNotSupportedException;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Transient;

public class EntityProcessor extends ClassProcessor {

    private static final String TAG = EntityProcessor.class.getName();
    private EntityModel entityModel;

    public EntityProcessor(Element el, StormEnvironment stormEnv) {
        super(el, stormEnv);
    }

    @Override
    public EntityModel getModel() {
        return this.entityModel;
    }

    @Override
    public void populateModel() {
        // TODO make more elegant
        Entity entity = this.typeElement.getAnnotation(Entity.class);
        if (entity != null) {
            this.entityModel = new EntityModel(entity);
        } else {
            javax.persistence.Entity jpaEntity = this.typeElement.getAnnotation(javax.persistence.Entity.class);
            if (jpaEntity != null) {
                this.entityModel = new EntityModel(jpaEntity);
            }
        }
        super.populateModel();
        this.entityModel.addImport(getQualifiedClassName());
        validateTableName(entityModel.getTableName());
        chooseDatabase(entityModel.getDbName());
        chooseBaseDao(entity);
        readFields(typeElement);
        inspectId();
        // TODO Verify >1 column. If only ID col, insert() will fail
    }

    private void validateTableName(String tableName) {
        if (tableName != null && tableName.length() > 0) {
            this.entityModel.setTableName(tableName);
        } else {
            this.entityModel.setTableName(getClassName());
        }
        if (!SqlUtil.isValidIdentifier(this.entityModel.getTableName())) {
            abort(tableName + " is not a valid identifier. Enclose SQL keywords with [].");
        }
    }

    protected void chooseBaseDao(Entity entity) {
        BaseDaoModel baseDao = getBaseDaoClass(entity);
        this.entityModel.setBaseDaoClass(baseDao);
    }

    protected void chooseDatabase(String dbName) {
        DatabaseModel defaultDb = stormEnv.getDefaultDb();
        if (dbName != null && dbName.length() > 0) {
            // Add db to entity model and vice versa
            DatabaseModel db = stormEnv.getDbByName(dbName);
            if (db != null) {
                this.entityModel.setDatabase(db);
            } else {
                abort("There is no @Database named " + dbName);
            }
        } else if (defaultDb != null) {
            this.entityModel.setDatabase(defaultDb);
        } else {
            abort("You must define at least one @Database");
        }
    }

    @Override
    protected void inspectField(VariableElement field) {
        Set<Modifier> modifiers = field.getModifiers();
        boolean hasId = (field.getAnnotation(Id.class) != null);
        String fieldName = field.getSimpleName().toString();
        if (!SqlUtil.isValidIdentifier(field.getSimpleName().toString())) {
            abort(fieldName + " is not a valid SQL column name.");
        }
        if (!modifiers.contains(Modifier.TRANSIENT) && field.getAnnotation(Transient.class) == null && !modifiers.contains(Modifier.STATIC)) {
            String javaType = getFieldType(field);
            if (TypeKind.DECLARED.equals(field.asType().getKind())) {
                DeclaredType type = (DeclaredType) field.asType();
                TypeElement typeElement = (TypeElement) type.asElement();
                TypeMirror superclass = typeElement.getSuperclass();
                if (ElementKind.ENUM.equals(typeElement.getKind())) {
                    if (hasId) {
                        abort("@Id invalid on enums", field);
                    } else {
                        FieldModel fm = new FieldModel(field.getSimpleName().toString(), javaType, true, stormEnv.getConverterForType("java.lang.Enum"));
                        entityModel.addField(fm);
                    }
                    return;
                }
            }
            // Verify supported type
            try {
                ConverterModel converter = stormEnv.getConverterForType(javaType);
                FieldModel f = new FieldModel(field.getSimpleName().toString(), javaType, false, converter);
                if (hasId) {
                    if (entityModel.getIdField() == null) {
                        if ("long".equals(f.getJavaType())) {
                            entityModel.setIdField(f);
                        } else {
                            abort("@Id field must be of type long", field);
                        }
                    } else {
                        abort("Duplicate @Id", field);
                    }
                }
                entityModel.addField(f);
            } catch (TypeNotSupportedException e) {
                stormEnv.getLogger().error(TAG + "inspectField", e, field);
            } catch (Exception e) {
                stormEnv.getLogger().error(TAG, e, field);
            }
            // TODO verify getter + setter
        } else if (hasId) {
            abort("@Id fields cannot be transient", field);
        }
    }

    /**
     * Verifies that the entity has exactly one id field of type long.
     */
    private void inspectId() {
        if (entityModel.getIdField() == null) {
            // Default to field named "id"
            List<FieldModel> fields = entityModel.getFields();
            for (FieldModel f : fields) {
                if (EntityModel.DEFAULT_ID_FIELD.equals(f.getFieldName())) {
                    entityModel.setIdField(f);
                }
            }
        }
        FieldModel idField = entityModel.getIdField();
        if (idField != null && "long".equals(idField.getJavaType())) {
            return;
        } else {
            abort("Entity must contain a field named id or annotated with @Id of type long");
        }
    }

    /**
     * Trying to get Class<?> from an annotation raises an exception
     * see http://stackoverflow.com/questions/7687829/java-6-annotation-processing-getting-a-class-from-an-annotation
     */
    private static TypeMirror getBaseDaoTypeMirror(Entity entity) {
        if(entity != null) {
            try {
                entity.baseDaoClass();
            } catch (MirroredTypeException mte) {
                return mte.getTypeMirror();
            }
        }
        return null;
    }

    /**
     * Builds a BaseDaoModel from the class passed as attribute baseDaoClass of the annotation Entity
     * @param entity
     * @return BaseDaoModel containing the package name + Class name
     */
    private static BaseDaoModel getBaseDaoClass(Entity entity) {
        String qualifiedName = SQLiteDao.class.getName();
        TypeMirror typeMirror = getBaseDaoTypeMirror(entity);
        if(typeMirror != null) qualifiedName = typeMirror.toString();
        return new BaseDaoModel(qualifiedName);
    }
}