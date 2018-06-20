/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.views.CustomWebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import timber.log.Timber;

/**
 * A base widget class which is responsible for sharing the code used by image map select widgets like
 * {@link SelectOneImageMapWidget} and {@link SelectMultiImageMapWidget}.
 */
public abstract class SelectImageMapWidget extends SelectWidget {
    private static final String WEB_VIEW_CONTENT =
            "<!DOCTYPE html> <html>\n" +
                    "    <body>\n" +
                    "           %s" + //inject an svg map here
                    "        <script src=\"file:///android_asset/svg_map_helper.js\"></script>\n" +
                    "    </body>\n" +
                    "</html>";

    private CustomWebView webView;
    private TextView selectedAreasLabel;

    private final boolean isSingleSelect;
    private String imageMapFilePath;
    protected List<Selection> selections = new ArrayList<>();

    public SelectImageMapWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        isSingleSelect = this instanceof SelectOneImageMapWidget;

        try {
            imageMapFilePath = ReferenceManager.instance().DeriveReference(prompt.getImageText()).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.w(e);
        }

        createLayout();
    }

    @Override
    public void clearAnswer() {
        selections.clear();
        webView.loadUrl("javascript:clearAreas()");
    }

    @Override
    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return webView.suppressFlingGesture();
    }

    private void createLayout() {
        readItems();

        webView = new CustomWebView(getContext());
        selectedAreasLabel = getAnswerTextView();
        answerLayout.addView(webView);
        answerLayout.addView(selectedAreasLabel);

        // add a space to facilitate scrolling
        int width = Math.round(getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density);
        int paddingInDp = width / 20; // 5% of the screen
        final float scale = getResources().getDisplayMetrics().density;
        int paddingInPx = (int) (paddingInDp * scale + 0.5f);
        answerLayout.setPadding(0, 0, paddingInPx, 0);

        addAnswerView(answerLayout);
        setUpWebView();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setUpWebView() {
        String svgMap = getParsedSVGFile();
        if (svgMap != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.addJavascriptInterface(new JavaScriptInterface(), "imageMapInterface");
            webView.loadDataWithBaseURL(null, String.format(WEB_VIEW_CONTENT, svgMap), "text/html", "UTF-8", null);
            webView.setInitialScale(1);
            webView.getSettings().setUseWideViewPort(true);
            int height = (int) (getResources().getDisplayMetrics().heightPixels / 1.7); // about 60% of a screen
            webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.loadUrl("javascript:setSelectMode(" + isSingleSelect + ")");
                    for (SelectChoice selectChoice : items) {
                        view.loadUrl("javascript:addArea('" + selectChoice.getValue() + "')");
                    }
                    highlightSelections(view);
                }
            });
        }
    }

    private void selectArea(String areaId) {
        SelectChoice selectChoice = null;
        for (SelectChoice sc : items) {
            if (areaId.equalsIgnoreCase(sc.getValue())) {
                selectChoice = sc;
            }
        }
        if (selectChoice != null) {
            selections.add(new Selection(selectChoice));
        }
    }

    private void unselectArea(String areaId) {
        Selection selectionToRemove = null;

        for (Selection selection : selections) {
            if (areaId.equalsIgnoreCase(selection.getValue())) {
                selectionToRemove = selection;
            }
        }

        selections.remove(selectionToRemove);
    }

    private void notifyChanges() {
        refreshSelectedItemsLabel();
    }

    private String getParsedSVGFile() {
        Document document;
        try {
            File initialFile = new File(imageMapFilePath);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new FileInputStream(initialFile));

            Element element = document.getDocumentElement();
            element.normalize();

            // Add default svg size if not specified
            NodeList nodeList = document.getElementsByTagName("svg");
            addSizeAttributesIfNeeded(nodeList);

            // Add onClick attributes
            nodeList = document.getElementsByTagName("g");
            addOnClickAttributes(nodeList);
            nodeList = document.getElementsByTagName("path");
            addOnClickAttributes(nodeList);
            nodeList = document.getElementsByTagName("rect");
            addOnClickAttributes(nodeList);
            nodeList = document.getElementsByTagName("circle");
            addOnClickAttributes(nodeList);
            nodeList = document.getElementsByTagName("ellipse");
            addOnClickAttributes(nodeList);
            nodeList = document.getElementsByTagName("polygon");
            addOnClickAttributes(nodeList);

        } catch (Exception e) {
            Timber.w(e);
            return getContext().getString(R.string.svg_file_does_not_exist);
        }

        return convertDocumentToString(document);
    }

    private void addOnClickAttributes(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Node elementId = node.getAttributes().getNamedItem("id");
            if (node.getNodeType() == Node.ELEMENT_NODE && elementId != null && doesElementExistInDataSet(elementId.getNodeValue())) {
                ((Element) node).setAttribute("onClick", "clickOnArea(this.id)");
            }
        }
    }

    private boolean doesElementExistInDataSet(String elementId) {
        for (SelectChoice item : items) {
            if (item.getValue().equals(elementId)) {
                return true;
            }
        }
        return false;
    }

    private void addSizeAttributesIfNeeded(NodeList nodes) {
        Node svg = nodes.item(0);
        if (svg.getAttributes().getNamedItem("width") == null) {
            ((Element) svg).setAttribute("width", "1000");
        }
        if (svg.getAttributes().getNamedItem("height") == null) {
            ((Element) svg).setAttribute("height", "1000");
        }
    }

    private static String convertDocumentToString(Document doc) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            Timber.w(e);
        }

        return null;
    }

    protected void refreshSelectedItemsLabel() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!selections.isEmpty()) {
            stringBuilder
                    .append("<b>")
                    .append(getContext().getString(R.string.selected))
                    .append("</b> ");
            for (Selection selection : selections) {
                String choiceName = getFormEntryPrompt().getSelectChoiceText(selection.choice);
                CharSequence choiceDisplayName = TextUtils.textToHtml(choiceName);
                stringBuilder.append(choiceDisplayName);
                if (selections.indexOf(selection) < selections.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
        }

        ((FormEntryActivity) getContext()).runOnUiThread(() ->
                selectedAreasLabel.setText(Html.fromHtml(stringBuilder.toString())));
    }

    protected abstract void highlightSelections(WebView view);

    private class JavaScriptInterface {
        @android.webkit.JavascriptInterface
        public void selectArea(String areaId) {
            SelectImageMapWidget.this.selectArea(areaId);
        }

        @android.webkit.JavascriptInterface
        public void unselectArea(String areaId) {
            SelectImageMapWidget.this.unselectArea(areaId);
        }

        @android.webkit.JavascriptInterface
        public void notifyChanges() {
            SelectImageMapWidget.this.notifyChanges();
        }
    }
}