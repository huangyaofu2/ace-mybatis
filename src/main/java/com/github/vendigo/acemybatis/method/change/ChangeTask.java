package com.github.vendigo.acemybatis.method.change;

import com.github.vendigo.acemybatis.config.AceConfig;
import com.github.vendigo.acemybatis.parser.ParamsHolder;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Task for inserting/updating/deleting bunch of entities in chunks.
 */
public class ChangeTask implements Callable<Integer> {
    private AceConfig config;
    private ChangeFunction changeFunction;
    private SqlSessionFactory sqlSessionFactory;
    private String statementName;
    private ParamsHolder params;

    public ChangeTask(AceConfig config, ChangeFunction changeFunction, SqlSessionFactory sqlSessionFactory, String statementName,
                      ParamsHolder params) {
        this.config = config;
        this.changeFunction = changeFunction;
        this.sqlSessionFactory = sqlSessionFactory;
        this.statementName = statementName;
        this.params = params;
    }

    @Override
    public Integer call() throws Exception {
        Integer i = 0;
        List<Object> entities = params.getEntities();
        Map<String, Object> otherParams = params.getOtherParams();

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            for (Object entity : entities) {
                changeFunction.apply(sqlSession, statementName, formatParam(entity, otherParams));
                i++;
                if (i % config.getUpdateChunkSize() == 0) {
                    sqlSession.commit();
                }
            }
            sqlSession.commit();
        }
        //In batch mode sqlSession returns incorrect number of affected rows. We presume that one call changes one row.
        return i;
    }

    private Object formatParam(Object entity, Map<String, Object> otherParams) {
        if (otherParams.isEmpty()) {
            return entity;
        } else {
            Map<String, Object> param = new HashMap<>();
            param.putAll(otherParams);
            param.put(config.getElementName(), entity);
            return param;
        }
    }
}
