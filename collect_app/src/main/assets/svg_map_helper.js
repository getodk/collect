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

/**
* JavaScript class which contains methods used for managing svg maps
*/
var selectedAreas = new Set();
var originalColors = new Map();
var lastSelectedAreaId;
var isSingleSelect;

function selectArea(areaId) {
    imageMapInterface.selectArea(areaId);
}

function unselectArea(areaId) {
    imageMapInterface.unselectArea(areaId);
}

function notifyChanges() {
    imageMapInterface.notifyChanges();
}

function addSelectedArea(selectedAreaId) {
    selectedAreas.add(selectedAreaId);
    document.getElementById(selectedAreaId).setAttribute('style', 'fill: #E65100');
    if (Boolean(isSingleSelect)) {
        lastSelectedAreaId = selectedAreaId;
    }
}

function addArea(areaId) {
    originalColors.set(areaId, document.getElementById(areaId).style.color);
}

function setSelectMode(isSingleSelect) {
    this.isSingleSelect = isSingleSelect;
}

function clickOnArea(areaId) {
    if (selectedAreas.has(areaId)) {
        document.getElementById(areaId).setAttribute('style', 'fill: ' + originalColors.get(areaId));
        selectedAreas.delete(areaId);
        unselectArea(areaId);
    } else {
        if (Boolean(isSingleSelect) && !!lastSelectedAreaId) {
            document.getElementById(lastSelectedAreaId).setAttribute('style', 'fill: ' + originalColors.get(lastSelectedAreaId));
            selectedAreas.delete(lastSelectedAreaId);
            unselectArea(lastSelectedAreaId);
        }
        document.getElementById(areaId).setAttribute('style', 'fill: #E65100');
        selectedAreas.add(areaId);
        selectArea(areaId);
        lastSelectedAreaId = areaId;
    }
    notifyChanges();
}