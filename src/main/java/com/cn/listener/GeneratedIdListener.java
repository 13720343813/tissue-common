package com.cn.listener;

import com.cn.annotation.GeneratedId;
import com.cn.dao.MongoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Component
public class GeneratedIdListener extends AbstractMongoEventListener<Object> {
    @Autowired
    private MongoDao mongoDao;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> source) {
        super.onBeforeConvert(source);
        if (source != null) {
            ReflectionUtils.doWithFields(source.getSource().getClass(), new ReflectionUtils.FieldCallback() {
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    if (field.isAnnotationPresent(GeneratedId.class)) {
                        if (null == field.get(source.getSource())) {
                            field.set(source.getSource(), mongoDao.getNextId(source.getSource().getClass().getName()));
                        }
                    }
                }
            });
        }
    }
}
