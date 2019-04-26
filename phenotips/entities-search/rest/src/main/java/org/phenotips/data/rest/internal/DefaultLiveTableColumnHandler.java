/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import com.gene42.commons.xwiki.XWikiTools;

import org.phenotips.data.api.internal.SearchUtils;
import org.phenotips.data.api.internal.filter.StringFilter;
import org.phenotips.data.rest.LiveTableColumnHandler;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.localization.LocalizationContext;
import org.xwiki.localization.LocalizationManager;
import org.xwiki.localization.Translation;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.classes.DBListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.ViewAction;

/**
 * This class generates a column for a live table row.
 *
 * @version $Id$
 */
@Component(roles = { LiveTableColumnHandler.class })
@Singleton
@SuppressWarnings({
    "checkstyle:classdataabstractioncoupling",
    "checkstyle:classfanoutcomplexity",
    "checkstyle:cyclomaticcomplexity",
    "checkstyle:executablestatementcount",
    "checkstyle:npathcomplexity"
    })
public class DefaultLiveTableColumnHandler implements LiveTableColumnHandler
{
    private static final String CUSTOM_DISPLAY = "customDisplay";

    private static final char MULTI_RESULT_DELIMITER = '|';

    @Inject
    private LocalizationContext localizationContext;

    @Inject
    private LocalizationManager localization;

    @Inject
    private Logger logger;

    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public void addColumn(JSONObject row, TableColumn col, XWikiDocument doc,
        Map<String, List<String>> queryParameters) throws XWikiException
    {
        if (EntityType.DOCUMENT.equals(col.getType())) {
            return;
        }

        String translationPrefix = StringUtils.EMPTY;
        if (queryParameters.containsKey(RequestUtils.TRANS_PREFIX_KEY)) {
            translationPrefix = RequestUtils.getFirst(queryParameters, RequestUtils.TRANS_PREFIX_KEY);
        }

        if (StringUtils.equals(col.getColName(), "_action")
            && queryParameters.containsKey(RequestUtils.TRANS_PREFIX_KEY)) {
            row.put(col.getColName(), this.localizationRender(translationPrefix + "actiontext", Syntax.PLAIN_1_0));
            return;
        }

        EntityReference wikiRef = SearchUtils.getClassDocumentReference(col.getClassName())
                                             .extractReference(EntityType.WIKI);

        ValueWrapper valueWrapper = new ValueWrapper();

        for (BaseObject obj : XWikiTools.getXObjects(doc, SearchUtils.getClassReference(col.getClassName()))) {
            this.handleXObject(valueWrapper, obj, doc, col, wikiRef, translationPrefix);
        }

        this.addColumnToRow(row, col.getColName(), valueWrapper);
    }

    private void handleXObject(ValueWrapper valueWrapper, BaseObject baseObject, XWikiDocument doc, TableColumn col,
        EntityReference wikiRef, String translationPrefix) throws XWikiException
    {
        if (baseObject == null) {
            return;
        }
        PropertyInterface field = baseObject.getField(col.getPropertyName());
        this.addDisplayValue(valueWrapper, baseObject, col, doc, field, translationPrefix);
        this.addValueAndURL(valueWrapper, baseObject, col, wikiRef, field);
    }

    private void addValueAndURL(ValueWrapper valueWrapper, BaseObject baseObject, TableColumn col,
        EntityReference wikiRef, PropertyInterface field)
    {
        String value = getStringValue(baseObject, col.getPropertyName());
        String valueURL = StringUtils.EMPTY;

        XWikiContext context = this.contextProvider.get();

        // DBListClass check also covers PageClass
        if (field instanceof DBListClass && StringUtils.isNotBlank(((DBListClass) field).getValueField())
            && !((DBListClass) field).isMultiSelect()) {

            DBListClass listField = (DBListClass) field;
            value = listField.getValueField();

            String testURL = context.getWiki().getURL(value, ViewAction.VIEW_ACTION, null, context);
            String compURL = context.getWiki().getURL(this.resolveDocument(
                StringUtils.EMPTY, this.componentManager, wikiRef), ViewAction.VIEW_ACTION, context);

            if (!StringUtils.equals(testURL, compURL)) {
                valueURL = testURL;
            }
        } else if (isFullReferenceMatch(col)) {

        } else if (StringUtils.startsWith(value, "xwiki:")) {
            String testURL = context.getWiki().getURL(value, ViewAction.VIEW_ACTION, null, context);
            String compURL = context.getWiki().getURL(this.resolveDocument(
                StringUtils.EMPTY, this.componentManager, wikiRef), ViewAction.VIEW_ACTION, context);

            if (!StringUtils.equals(testURL, compURL)) {
                valueURL = testURL;
            }
        }

        CollectionUtils.addIgnoreNull(valueWrapper.values, StringUtils.defaultIfEmpty(value, null));
        CollectionUtils.addIgnoreNull(valueWrapper.urls, StringUtils.defaultIfEmpty(valueURL, null));
    }

    private static boolean isFullReferenceMatch(TableColumn col) {
        return col.getFilters()
           .stream()
           .map(f -> f.optString(StringFilter.MATCH_KEY, null))
           .filter(Objects::nonNull)
           .anyMatch(StringFilter.MATCH_FULL_REFERENCE::equals);
    }

    private void addDisplayValue(ValueWrapper valueWrapper, BaseObject baseObject, TableColumn col,
        XWikiDocument doc, PropertyInterface field, String translationPrefix)
        throws XWikiException
    {
        String customDisplay;

        if (field instanceof PropertyClass) {
            customDisplay = ((PropertyClass) field).getCustomDisplay();
        } else {
            customDisplay = getStringValue(baseObject, CUSTOM_DISPLAY);
        }

        XWikiContext context = this.contextProvider.get();

        String displayValue = doc.display(
            col.getColName(),
            ViewAction.VIEW_ACTION,
            StringUtils.EMPTY,
            baseObject,
            context.getWiki().getCurrentContentSyntaxId(doc.getSyntax().toIdString(), context),
            context);

        if (isStringValue(field, customDisplay)) {
            XDOM parsedValue = this.parse(displayValue, Syntax.HTML_4_01.toIdString());
            displayValue = this.render(parsedValue, Syntax.PLAIN_1_0.toIdString(), this.componentManager);
        }

        if (StringUtils.isBlank(displayValue)) {
            displayValue = this.getEmptyDisplayValue(translationPrefix);
        }

        displayValue = displayValue.replaceFirst(Pattern.quote("{{html clean=\"false\" wiki=\"false\"}}"), "");
        displayValue = displayValue.replaceAll(Pattern.quote("{{/html}}"), "");

        CollectionUtils.addIgnoreNull(valueWrapper.displayValues, StringUtils.defaultIfEmpty(displayValue, null));
    }

    private static boolean isStringValue(PropertyInterface field, String customDisplay)
    {
        // StringClass check also covers TextAreaClass
        boolean isFiledStr = field instanceof StringClass || field instanceof StringProperty;

        return StringUtils.isNotBlank(customDisplay) || field == null || isFiledStr;
    }

    private String getEmptyDisplayValue(String translationPrefix)
        throws XWikiException
    {
        return localizationRender(translationPrefix + "emptyvalue", Syntax.PLAIN_1_0);
    }

    private void addColumnToRow(JSONObject row, String columnName, ValueWrapper valueWrapper)
    {
        row.put(columnName, valueWrapper.getDisplayValue());
        row.put(columnName + "_value", valueWrapper.getValue());
        row.put(columnName + "_url", valueWrapper.getURL());
    }

    private String render(Block block, String outputSyntaxId, ComponentManager componentManager)
    {
        if (block == null) {
            return StringUtils.EMPTY;
        }
        String result;
        WikiPrinter printer = new DefaultWikiPrinter();
        try {
            BlockRenderer renderer = componentManager.getInstance(BlockRenderer.class, outputSyntaxId);
            renderer.render(block, printer);
            result = printer.toString();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            result = StringUtils.EMPTY;
        }
        return result;
    }

    private XDOM parse(String displayValue, String syntaxId)
    {
        try {
            Parser parser = this.componentManager.getInstance(Parser.class, syntaxId);
            return parser.parse(new StringReader(displayValue));
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            return null;
        }
    }

    private String localizationRender(String key, Syntax syntax)
        throws XWikiException
    {
        String result;

        Locale currentLocale = this.localizationContext.getCurrentLocale();

        Translation translation = this.localization.getTranslation(key, currentLocale);

        if (translation != null) {
            Block block = translation.render(currentLocale);

            // Render the block
            try {
                BlockRenderer renderer = this.componentManager.getInstance(BlockRenderer.class, syntax.toIdString());

                DefaultWikiPrinter wikiPrinter = new DefaultWikiPrinter();
                renderer.render(block, wikiPrinter);

                result = wikiPrinter.toString();
            } catch (ComponentLookupException e) {
                throw new XWikiException(
                    String.format("[%1$s] component could not be instantiated.", BlockRenderer.class.toString()), e);
            }
        } else {
            result = key;
        }

        return result;
    }

    private static String getStringValue(BaseObject baseObject, String propertyName)
    {
        if (baseObject == null) {
            return "";
        } else {
            String result = baseObject.getStringValue(propertyName);
            return " ".equals(result) ? "" : result;
        }
    }

    private DocumentReference resolveDocument(String stringRepresentation, Object... parameters)
    {
        try {
            EntityReferenceResolver<String> resolver =
                this.componentManager.getInstance(EntityReferenceResolver.TYPE_STRING, "default");
            return new DocumentReference(resolver.resolve(stringRepresentation, EntityType.DOCUMENT, parameters));
        } catch (ComponentLookupException e) {
            this.logger.warn(e.getMessage(), e);
            return null;
        }
    }

    private static class ValueWrapper
    {
        protected List<String> displayValues = new LinkedList<>();
        protected List<String> values = new LinkedList<>();
        protected List<String> urls = new LinkedList<>();

        protected String getDisplayValue()
        {
            return joinList(this.displayValues);
        }

        protected String getValue()
        {
            return joinList(this.values);
        }

        protected String getURL()
        {
            return joinList(this.urls);
        }

        private static String joinList(List<String> list)
        {
            if (CollectionUtils.isEmpty(list)) {
                return StringUtils.EMPTY;
            } else {
                return StringUtils.join(list, MULTI_RESULT_DELIMITER);
            }
        }
    }
}

