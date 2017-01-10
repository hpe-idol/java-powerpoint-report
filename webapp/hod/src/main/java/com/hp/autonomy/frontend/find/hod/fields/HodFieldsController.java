/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsService;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Controller
@Slf4j
class HodFieldsController extends FieldsController<HodFieldsRequest, HodErrorException, HodQueryRestrictions, HodParametricRequest> {
    static final String DATABASES_PARAM = "databases";

    private final ObjectFactory<HodFieldsRequestBuilder> fieldsRequestBuilderFactory;
    private final ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    HodFieldsController(
            final HodFieldsService fieldsService,
            final HodParametricValuesService parametricValuesService,
            final ObjectFactory<HodParametricRequestBuilder> parametricRequestBuilderFactory,
            final TagNameFactory tagNameFactory,
            final ConfigService<? extends FindConfig<?, ?>> configService,
            final ObjectFactory<HodFieldsRequestBuilder> fieldsRequestBuilderFactory,
            final ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory) {
        super(fieldsService, parametricValuesService, parametricRequestBuilderFactory, tagNameFactory, configService);
        this.fieldsRequestBuilderFactory = fieldsRequestBuilderFactory;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
    }

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<TagName> getParametricFields(@RequestParam(DATABASES_PARAM) final Collection<ResourceIdentifier> databases) throws HodErrorException {
        return getParametricFields(fieldsRequestBuilderFactory.getObject()
                .databases(databases)
                .build());
    }

    @RequestMapping(value = GET_PARAMETRIC_NUMERIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails> getParametricNumericFields(@RequestParam(DATABASES_PARAM) final Collection<ResourceIdentifier> databases) throws HodErrorException {
        return getParametricNumericFields(fieldsRequestBuilderFactory.getObject()
                .databases(databases)
                .build());
    }

    @RequestMapping(value = GET_PARAMETRIC_DATE_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails> getParametricDateFields(@RequestParam(DATABASES_PARAM) final Collection<ResourceIdentifier> databases) throws HodErrorException {
        return getParametricDateFields(fieldsRequestBuilderFactory.getObject()
                .databases(databases)
                .build());
    }

    @Override
    protected HodQueryRestrictions createValueDetailsQueryRestrictions(final HodFieldsRequest request) {
        return queryRestrictionsBuilderFactory.getObject()
                .queryText("*")
                .databases(new LinkedList<>(request.getDatabases()))
                .build();
    }

    @Override
    protected List<FieldAndValueDetails> getParametricDateFields(final HodFieldsRequest request) throws HodErrorException {
        // TODO: Remove this override once FIND-180 is complete; we are just preventing AUTN_DATE from showing up in HoD as it will cause performance problems
        return fetchParametricFieldAndValueDetails(request, FieldTypeParam.NumericDate, Collections.emptyList());
    }
}
