package com.tencent.bkrepo.common.query.builder

import com.tencent.bkrepo.common.query.enums.OperationType
import com.tencent.bkrepo.common.query.exception.QueryModelException
import com.tencent.bkrepo.common.query.handler.MongoQueryRuleHandler
import com.tencent.bkrepo.common.query.handler.impl.AfterHandler
import com.tencent.bkrepo.common.query.handler.impl.BeforeHandler
import com.tencent.bkrepo.common.query.handler.impl.DefaultMongoNestedRuleHandler
import com.tencent.bkrepo.common.query.handler.impl.EqualHandler
import com.tencent.bkrepo.common.query.handler.impl.GreaterThanHandler
import com.tencent.bkrepo.common.query.handler.impl.GreaterThanOrEqualHandler
import com.tencent.bkrepo.common.query.handler.impl.InHandler
import com.tencent.bkrepo.common.query.handler.impl.LessThanHandler
import com.tencent.bkrepo.common.query.handler.impl.LessThanOrEqualHandler
import com.tencent.bkrepo.common.query.handler.impl.MatchHandler
import com.tencent.bkrepo.common.query.handler.impl.NotEqualHandler
import com.tencent.bkrepo.common.query.handler.impl.NotNullHandler
import com.tencent.bkrepo.common.query.handler.impl.NullHandler
import com.tencent.bkrepo.common.query.handler.impl.PrefixHandler
import com.tencent.bkrepo.common.query.handler.impl.SuffixHandler
import com.tencent.bkrepo.common.query.interceptor.QueryContext
import com.tencent.bkrepo.common.query.interceptor.QueryModelInterceptor
import com.tencent.bkrepo.common.query.interceptor.QueryRuleInterceptor
import com.tencent.bkrepo.common.query.model.QueryModel
import com.tencent.bkrepo.common.query.model.Rule
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

/**
 * MongoDB QueryInterpreter
 */
open class MongoQueryInterpreter {

    private val defaultQueryRuleHandlerMap = mutableMapOf<OperationType, MongoQueryRuleHandler>()
    private val nestedRuleHandler = DefaultMongoNestedRuleHandler()

    private val queryRuleInterceptorList = mutableListOf<QueryRuleInterceptor>()
    private val queryModelInterceptorList = mutableListOf<QueryModelInterceptor>()

    init {
        defaultQueryRuleHandlerMap[OperationType.EQ] = EqualHandler()
        defaultQueryRuleHandlerMap[OperationType.NE] = NotEqualHandler()
        defaultQueryRuleHandlerMap[OperationType.LT] = LessThanHandler()
        defaultQueryRuleHandlerMap[OperationType.LTE] = LessThanOrEqualHandler()
        defaultQueryRuleHandlerMap[OperationType.GT] = GreaterThanHandler()
        defaultQueryRuleHandlerMap[OperationType.GTE] = GreaterThanOrEqualHandler()
        defaultQueryRuleHandlerMap[OperationType.BEFORE] = BeforeHandler()
        defaultQueryRuleHandlerMap[OperationType.AFTER] = AfterHandler()
        defaultQueryRuleHandlerMap[OperationType.IN] = InHandler()
        defaultQueryRuleHandlerMap[OperationType.PREFIX] = PrefixHandler()
        defaultQueryRuleHandlerMap[OperationType.SUFFIX] = SuffixHandler()
        defaultQueryRuleHandlerMap[OperationType.MATCH] = MatchHandler()
        defaultQueryRuleHandlerMap[OperationType.NULL] = NullHandler()
        defaultQueryRuleHandlerMap[OperationType.NOT_NULL] = NotNullHandler()
    }

    open fun interpret(queryModel: QueryModel): QueryContext {
        val mongoQuery = Query()
        val queryContext = initContext(queryModel, mongoQuery)
        var newModel = queryModel
        for (interceptor in queryModelInterceptorList) {
            newModel = interceptor.intercept(newModel, queryContext)
            queryContext.queryModel = newModel
        }
        val pageNumber = newModel.page.getNormalizedPageNumber()
        val pageSize = newModel.page.getNormalizedPageSize()
        newModel.page.let { mongoQuery.with(PageRequest.of(pageNumber - 1, pageSize)) }
        newModel.sort?.let { mongoQuery.with(Sort.by(Sort.Direction.fromString(it.direction.name), *it.properties.toTypedArray())) }
        newModel.select?.forEach { mongoQuery.fields().include(it) }

        mongoQuery.addCriteria(resolveRule(queryModel.rule, queryContext))
        return queryContext
    }

    open fun initContext(queryModel: QueryModel, mongoQuery: Query): QueryContext {
        return QueryContext(queryModel, mongoQuery, this)
    }

    fun addRuleInterceptor(interceptor: QueryRuleInterceptor) {
        this.queryRuleInterceptorList.add(interceptor)
    }

    fun addModelInterceptor(interceptor: QueryModelInterceptor) {
        this.queryModelInterceptorList.add(interceptor)
    }

    fun resolveRule(rule: Rule, context: QueryContext): Criteria {
        // interceptor
        if (rule !is Rule.FixedRule) {
            for (interceptor in queryRuleInterceptorList) {
                if (interceptor.match(rule)) {
                    return interceptor.intercept(rule, context)
                }
            }
        }
        // resolve
        return when (rule) {
            is Rule.NestedRule -> resolveNestedRule(rule, context)
            is Rule.QueryRule -> resolveQueryRule(rule)
            is Rule.FixedRule -> resolveQueryRule(rule.wrapperRule)
        }
    }

    private fun resolveNestedRule(rule: Rule.NestedRule, context: QueryContext): Criteria {
        return nestedRuleHandler.handle(rule, context)
    }

    private fun resolveQueryRule(rule: Rule.QueryRule): Criteria {
        // 默认handler
        return findDefaultHandler(rule.operation).handle(rule)
    }

    private fun findDefaultHandler(operation: OperationType): MongoQueryRuleHandler {
        return defaultQueryRuleHandlerMap[operation] ?: throw QueryModelException("Unsupported operation [$operation].")
    }
}
