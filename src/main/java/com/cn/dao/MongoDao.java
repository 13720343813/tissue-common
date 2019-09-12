package com.cn.dao;

import com.cn.annotation.Sequence;
import com.cn.helper.CommonDaoHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Repository
public class MongoDao {
    @Autowired(required = false)
    private MongoTemplate mongoTemplate;
    private Class entityClass;
    private String collectionName;
    private String orderAscField;
    private String orderDescField;

    public MongoDao() {
    }

    public MongoDao(Class entityClass) {
        this.entityClass = entityClass;
        this.collectionName = this._getCollectionName();
    }

    public MongoDao(Class entityClass, String collectionName) {
        this.entityClass = entityClass;
        this.collectionName = collectionName;
    }

    public Long count() {
        return mongoTemplate.count(new Query(), collectionName);
    }

    public Long count(Criteria criteria) {
        return mongoTemplate.count(new Query(criteria), collectionName);
    }

    public Object find(Criteria criteria) {
        Query query = new Query(criteria);
        _sort(query);
        return mongoTemplate.find(query, entityClass, collectionName);
    }

    public Object find(Criteria criteria, Integer pageSize) {
        Query query = new Query(criteria).limit(pageSize);
        _sort(query);
        return mongoTemplate.find(query, entityClass, collectionName);
    }

    public Object find(Criteria criteria, Integer pageSize, Integer pageNumber) {
        Query query = new Query(criteria).skip((pageNumber - 1) * pageSize).limit(pageSize);
        _sort(query);
        return mongoTemplate.find(query, entityClass, collectionName);
    }

    public Object findOne(Criteria criteria) {
        Query query = new Query(criteria).limit(1);
        _sort(query);
        return mongoTemplate.findOne(query, entityClass, collectionName);
    }

    public Object findOne(Criteria criteria, Integer skip) {
        Query query = new Query(criteria).skip(skip).limit(1);
        _sort(query);
        return mongoTemplate.findOne(query, entityClass, collectionName);
    }

    public Object findOne(Integer skip) {
        Query query = new Query().skip(skip).limit(1);
        _sort(query);
        return mongoTemplate.findOne(query, entityClass, collectionName);
    }

    public List fetchCollection(Map<String, Object> requestArgs) {
        Criteria criteria = getRequestRestriction((Map<String, Object>) requestArgs.get("query"));
        String sortField = CommonDaoHelper.getRequestSortField(requestArgs);
        String sortDirection = CommonDaoHelper.getRequestSortDirection(requestArgs);
        Integer pageSize = CommonDaoHelper.getRequestPageSize(requestArgs);
        Integer pageNumber = CommonDaoHelper.getRequestPageNumber(requestArgs);
        if ("-1".equals(sortDirection)) {
            this.setOrderDescField(sortField);
            this.setOrderAscField((String) null);
        } else {
            this.setOrderAscField(sortField);
            this.setOrderDescField((String) null);
        }
        return (List) find(criteria, pageSize, pageNumber);
    }

    public Long fetchCollectionCount(Map<String, Object> requestArgs) {
        Criteria criteria = getRequestRestriction((Map<String, Object>) requestArgs.get("query"));
        return count(criteria);
    }

    public Boolean update(Map<String, Object> requestArgs) {
        Object id = requestArgs.get("id");
        if (null == id) {
            return false;
        } else {
            try {
                Update update = new Update();
                Map<String, Object> updates = (Map<String, Object>) requestArgs.get("update");
                updates.remove("id");
                updates.remove("class");
                Iterator iterator = updates.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    update.set(key, updates.get(key));
                }
                findAndModify(Criteria.where("id").is(id), update);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static Criteria getRequestRestriction(Map<String, Object> query) {
        Criteria allCriteria = new Criteria();
        List<Criteria> criterias = new ArrayList();
        if (null != query) {
            Map<String, Object> elemQuery = new HashMap();
            Map<String, Object> newQuery = new HashMap();
            Iterator var5 = query.keySet().iterator();

            String key;
            while (var5.hasNext()) {
                key = (String) var5.next();
                if (key.contains(".")) {
                    String[] keys = key.split("\\.");
                    String key1 = keys[0];
                    String key2 = keys[1];
                    if (elemQuery.containsKey(key1)) {
                        Map<String, Object> keyMap = (Map) elemQuery.get(key1);
                        keyMap.put(key2, query.get(key1));
                        elemQuery.put(key1, keyMap);
                    } else {
                        Map<String, Object> keyMap = new HashMap();
                        keyMap.put(key2, query.get(key1));
                        elemQuery.put(key1, keyMap);
                    }
                } else {
                    newQuery.put(key, query.get(key));
                }
            }

            var5 = elemQuery.keySet().iterator();

            Map orValues;
            Object value;
            while (var5.hasNext()) {
                key = (String) var5.next();
                if ("$or".equals(key)) {
                    orValues = (Map) elemQuery.get(key);
                    criterias.add(_parseRequestRestrictionOr(orValues));
                } else {
                    value = elemQuery.get(key);
                    criterias.addAll(_parseCriteria((Map) value, key));
                }
            }

            var5 = newQuery.keySet().iterator();

            while (var5.hasNext()) {
                key = (String) var5.next();
                if ("$or".equals(key)) {
                    orValues = (Map) query.get(key);
                    criterias.add(_parseRequestRestrictionOr(orValues));
                } else {
                    value = query.get(key);
                    criterias.addAll(_parseCriteria(key, value));
                }
            }

            if (!criterias.isEmpty()) {
                allCriteria.andOperator((Criteria[]) ((Criteria[]) criterias.toArray(new Criteria[criterias.size()])));
            }
        }

        return allCriteria;
    }

    private static List<Criteria> _parseCriteriaOr(String key, Object value) {
        if ("id".equals(key)) {
            key = "_id";
        }

        List<Criteria> criterias = new ArrayList();
        if (value instanceof Map) {
            Map<String, Object> compareValue = (Map) value;
            Iterator var4 = compareValue.keySet().iterator();

            while (var4.hasNext()) {
                String compare = (String) var4.next();
                Object _compareValue = compareValue.get(compare);
                if ("$ge".equals(compare)) {
                    criterias.add(Criteria.where(key).gte(_compareValue));
                } else if ("$le".equals(compare)) {
                    criterias.add(Criteria.where(key).lte(_compareValue));
                } else if ("$gt".equals(compare)) {
                    criterias.add(Criteria.where(key).gt(_compareValue));
                } else if ("$lt".equals(compare)) {
                    criterias.add(Criteria.where(key).lt(_compareValue));
                } else if ("$in".equals(compare)) {
                    criterias.add(Criteria.where(key).in((Collection) _compareValue));
                } else if ("$like".equals(compare)) {
                    criterias.add(Criteria.where(key).regex(Pattern.compile(Pattern.quote((String) _compareValue), 2)));
                } else if ("$left_like".equals(compare)) {
                    criterias.add(Criteria.where(key).regex(Pattern.compile(Pattern.quote((String) _compareValue + "$"), 2)));
                } else if ("$right_like".equals(compare)) {
                    criterias.add(Criteria.where(key).regex(Pattern.compile(Pattern.quote("^" + (String) _compareValue), 2)));
                } else if ("$not_like".equals(compare)) {
                    criterias.add(Criteria.where(key).not().regex((String) _compareValue));
                } else if ("$left_like".equals(compare)) {
                    criterias.add(Criteria.where(key).not().regex(Pattern.compile(Pattern.quote((String) _compareValue + "$"), 2)));
                } else if ("$not_right_like".equals(compare)) {
                    criterias.add(Criteria.where(key).not().regex(Pattern.compile(Pattern.quote("^" + (String) _compareValue), 2)));
                } else if ("$ne".equals(compare)) {
                    criterias.add(Criteria.where(key).ne(_compareValue));
                } else if ("$null".equals(compare)) {
                    criterias.add(Criteria.where(key).is((Object) null));
                } else if ("$not_null".equals(compare)) {
                    criterias.add(Criteria.where(key).not().is((Object) null));
                } else if ("$not_in".equals(compare)) {
                    criterias.add(Criteria.where(key).not().in((Collection) _compareValue));
                } else if ("$where".equals(compare)) {
                    criterias.add(Criteria.where("$where").is(_compareValue));
                }
            }
        } else {
            criterias.add(Criteria.where(key).is(value));
        }

        return criterias;
    }

    private static Criteria _parseRequestRestrictionOr(Map<String, Object> query) {
        Criteria allOrCriteria = new Criteria();
        List<Criteria> criterias = new ArrayList();
        if (null != query) {
            Iterator var3 = query.keySet().iterator();

            while (var3.hasNext()) {
                String key = (String) var3.next();
                Object value = query.get(key);
                if (StringUtils.startsWith(key, "$and")) {
                    criterias.add(getRequestRestriction((Map) value));
                } else {
                    criterias.addAll(_parseCriteriaOr(key, value));
                }
            }
        }

        if (!criterias.isEmpty()) {
            allOrCriteria.orOperator((Criteria[]) criterias.toArray(new Criteria[criterias.size()]));
        }

        return allOrCriteria;
    }

    private static List<Criteria> _parseCriteria(String key, Object value) {
        if ("id".equals(key)) {
            key = "_id";
        }

        List criterias = new ArrayList();
        Criteria criteriaObj = null;
        Criteria criteria = null;
        boolean isElem = false;
        if (key.contains(".")) {
            String[] keys = key.split("\\.");
            criteriaObj = Criteria.where(keys[0]);
            criteria = Criteria.where(keys[1]);
            isElem = true;
        } else {
            criteria = Criteria.where(key);
        }

        if (value instanceof Map) {
            Map<String, String> compareValue = (Map) value;
            Iterator var7 = compareValue.keySet().iterator();

            while (var7.hasNext()) {
                String compare = (String) var7.next();
                Object _compareValue = compareValue.get(compare);
                if ("$ge".equals(compare)) {
                    criteria.gte(_compareValue);
                } else if ("$le".equals(compare)) {
                    criteria.lte(_compareValue);
                } else if ("$gt".equals(compare)) {
                    criteria.gt(_compareValue);
                } else if ("$lt".equals(compare)) {
                    criteria.lt(_compareValue);
                } else if ("$in".equals(compare)) {
                    criteria.in((Collection) _compareValue);
                } else if ("$like".equals(compare)) {
                    criteria.regex(Pattern.compile(Pattern.quote((String) _compareValue), 2));
                } else if ("$left_like".equals(compare)) {
                    criteria.regex(Pattern.compile(Pattern.quote((String) _compareValue + "$"), 2));
                } else if ("$right_like".equals(compare)) {
                    criteria.regex(Pattern.compile(Pattern.quote("^" + (String) _compareValue), 2));
                } else if ("$not_like".equals(compare)) {
                    criteria.not().regex((String) _compareValue);
                } else if ("$left_like".equals(compare)) {
                    criteria.not().regex(Pattern.compile(Pattern.quote((String) _compareValue + "$"), 2));
                } else if ("$not_right_like".equals(compare)) {
                    criteria.not().regex(Pattern.compile(Pattern.quote("^" + (String) _compareValue), 2));
                } else if ("$ne".equals(compare)) {
                    criteria.ne(_compareValue);
                } else if ("$null".equals(compare)) {
                    criteria.is((Object) null);
                } else if ("$not_null".equals(compare)) {
                    criteria.not().is((Object) null);
                } else if ("$not_in".equals(compare)) {
                    criteria.not().in((Collection) _compareValue);
                } else if ("$where".equals(compare)) {
                    criteria.is(_compareValue);
                }
            }
        } else {
            criteria.is(value);
        }

        if (criteria != null && isElem) {
            criteriaObj.elemMatch(criteria);
            criterias.add(criteriaObj);
        } else {
            criterias.add(criteria);
        }

        return criterias;
    }

    private static List<Criteria> _parseCriteria(Map<String, Object> criMap, String key) {
        if ("id".equals(key)) {
            key = "_id";
        }

        List criterias = new ArrayList();
        Criteria criteriaObj = Criteria.where(key);
        Criteria criteria = null;
        Iterator var5 = criMap.keySet().iterator();

        while (true) {
            while (var5.hasNext()) {
                String filedKey = (String) var5.next();
                if (criteria == null) {
                    criteria = Criteria.where(filedKey);
                } else {
                    criteria = criteria.and(filedKey);
                }

                Object value = criMap.get(filedKey);
                if (value instanceof Map) {
                    Map<String, String> compareValue = (Map) value;
                    Iterator var9 = compareValue.keySet().iterator();

                    while (var9.hasNext()) {
                        String compare = (String) var9.next();
                        Object _compareValue = compareValue.get(compare);
                        if ("$ge".equals(compare)) {
                            criteria.gte(_compareValue);
                        } else if ("$le".equals(compare)) {
                            criteria.lte(_compareValue);
                        } else if ("$gt".equals(compare)) {
                            criteria.gt(_compareValue);
                        } else if ("$lt".equals(compare)) {
                            criteria.lt(_compareValue);
                        } else if ("$in".equals(compare)) {
                            criteria.in((Collection) _compareValue);
                        } else if ("$like".equals(compare)) {
                            criteria.regex(Pattern.compile(Pattern.quote((String) _compareValue), 2));
                        } else if ("$left_like".equals(compare)) {
                            criteria.regex(Pattern.compile(Pattern.quote((String) _compareValue + "$"), 2));
                        } else if ("$right_like".equals(compare)) {
                            criteria.regex(Pattern.compile(Pattern.quote("^" + (String) _compareValue), 2));
                        } else if ("$not_like".equals(compare)) {
                            criteria.not().regex((String) _compareValue);
                        } else if ("$left_like".equals(compare)) {
                            criteria.not().regex(Pattern.compile(Pattern.quote((String) _compareValue + "$"), 2));
                        } else if ("$not_right_like".equals(compare)) {
                            criteria.not().regex(Pattern.compile(Pattern.quote("^" + (String) _compareValue), 2));
                        } else if ("$ne".equals(compare)) {
                            criteria.ne(_compareValue);
                        } else if ("$null".equals(compare)) {
                            criteria.is((Object) null);
                        } else if ("$not_null".equals(compare)) {
                            criteria.not().is((Object) null);
                        } else if ("$not_in".equals(compare)) {
                            criteria.not().in((Collection) _compareValue);
                        } else if ("$where".equals(compare)) {
                            criteria.is(_compareValue);
                        }
                    }
                } else {
                    criteria.is(value);
                }
            }

            criteriaObj.elemMatch(criteria);
            criterias.add(criteriaObj);
            return criterias;
        }
    }

    public Object findAndModify(Criteria criteria, Update update) {
        return mongoTemplate.findAndModify(new Query(criteria), update, entityClass, collectionName);
    }

    public void _sort(Query query) {
        String[] fields;
        if (null != orderAscField) {
            fields = orderAscField.split("\\,");
            for (String field : fields) {
                if ("id".equals(field)) {
                    field = "_id";
                }
                query.with(new Sort(Sort.Direction.ASC, new String[]{field}));
            }
        } else if (null != orderDescField) {
            fields = orderDescField.split("\\,");
            for (String field : fields) {
                if ("id".equals(field)) {
                    field = "_id";
                }
                query.with(new Sort(Sort.Direction.DESC, new String[]{field}));
            }
        }
    }

    public Object findById(Object id) {
        return mongoTemplate.findById(id, this.entityClass, this.collectionName);
    }

    public Object load(Object id) {
        return findById(id);
    }

    public Boolean deleteById(Object id) {
        Object object = load(id);
        return null == object ? false : remove(object);
    }

    public Boolean remove(Object object) {
        try {
            mongoTemplate.remove(object);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean saveOrUpdate(Object object) {
        try {
            mongoTemplate.save(object, collectionName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public Long getNextId() {
        return getNextId(_getCollectionName());
    }

    public Long getNextId(String collName) {
        Query query = new Query(Criteria.where("collectionName").is(collName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        Sequence sequence = mongoTemplate.findAndModify(query, update, options, Sequence.class);
        return sequence.getSeqId();
    }

    private String _getCollectionName() {
        String className = this.entityClass.getName();
        Integer lastIndex = className.lastIndexOf(".");
        className = className.substring(lastIndex + 1);
        return StringUtils.uncapitalize(className);
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getOrderAscField() {
        return orderAscField;
    }

    public void setOrderAscField(String orderAscField) {
        this.orderAscField = orderAscField;
    }

    public String getOrderDescField() {
        return orderDescField;
    }

    public void setOrderDescField(String orderDescField) {
        this.orderDescField = orderDescField;
    }
}
