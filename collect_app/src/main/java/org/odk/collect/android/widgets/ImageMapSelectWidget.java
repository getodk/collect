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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.TextUtils;
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

public abstract class ImageMapSelectWidget extends SelectWidget {
    private static final String WEB_VIEW_CONTENT =
            "<!DOCTYPE html> <html>\n" +
                    "    <body>\n" +
                    "           %s" + //inject an svg map here
                    "        <script src=\"file:///android_asset/svg_map_helper.js\"></script>\n" +
                    "    </body>\n" +
                    "</html>";

    private WebView webView;
    private TextView selectedAreasLabel;

    private String imageMapFilePath;
    protected List<Selection> selections = new ArrayList<>();

    public ImageMapSelectWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        try {
            imageMapFilePath = ReferenceManager.instance().DeriveReference(prompt.getImageText()).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.w(e);
        }

        setUpLayout();
    }

    @Override
    public void clearAnswer() {
        webView.loadUrl("javascript:clearAreas()");
    }

    private void setUpLayout() {
        webView = new WebView(getContext());
        selectedAreasLabel = getAnswerTextView();
        answerLayout.addView(webView);
        answerLayout.addView(selectedAreasLabel);
        addAnswerView(answerLayout);

        setUpWebView();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setUpWebView() {
        String svgMap = getParsedSVGFile();
        if (svgMap != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.addJavascriptInterface(new JavaScriptInterface(), "imageMapInterface");
            webView.loadDataWithBaseURL(null, String.format(WEB_VIEW_CONTENT, svgMap), "text/html", "UTF-8", null);
            webView.setInitialScale(1);
            webView.getSettings().setUseWideViewPort(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    for (SelectChoice selectChoice : items) {
                        view.loadUrl("javascript:addArea('" + selectChoice.getValue() + "')");
                    }
                    adjustWebView(view);
                }
            });
        }
    }

    public void onAreaClick(String areaId) {
        if (isAreaSelected(areaId)) {
            removeSelection(areaId);
        } else {
            SelectChoice selectChoice = getSelectedChoiceByAreaId(areaId);
            if (selectChoice != null) {
                selections.add(new Selection(selectChoice));
            }
        }

        refreshSelectedItemsLabel();
    }

    private boolean isAreaSelected(String areaId) {
        for (Selection selection : selections) {
            if (areaId.equalsIgnoreCase(selection.getValue())) {
                return true;
            }
        }
        return false;
    }

    private void removeSelection(String areaId) {
        Selection selectionToRemove = null;

        for (Selection selection : selections) {
            if (areaId.equalsIgnoreCase(selection.getValue())) {
                selectionToRemove = selection;
            }
        }

        selections.remove(selectionToRemove);
    }

    private SelectChoice getSelectedChoiceByAreaId(String areaId) {
        for (SelectChoice selectChoice : items) {
            if (areaId.equalsIgnoreCase(selectChoice.getValue())) {
                return selectChoice;
            }
        }

        return null;
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

            NodeList nodeList = document.getElementsByTagName("g");
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
            return getContext().getString(R.string.image_map_does_not_exist);
        }

        return convertDocumentToString(document);
    }

    private void addOnClickAttributes(NodeList node) {
        for (int i = 0; i < node.getLength(); i++) {
            Node path = node.item(i);
            if (path.getNodeType() == Node.ELEMENT_NODE && path.getAttributes().getNamedItem("id") != null) {
                ((Element) path).setAttribute("onClick","clickOnArea(this.id)");
            }
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

    protected abstract void adjustWebView(WebView view);

    private class JavaScriptInterface {
        @android.webkit.JavascriptInterface
        public void onAreaClick(String areaId) {
            ImageMapSelectWidget.this.onAreaClick(areaId);
        }
    }
}