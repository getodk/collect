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
var selectedAreas = [];
var originalColors = {};
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

function clearAreas() {
    selectedAreas.forEach(function(areaId) {
        if (selectedAreas.indexOf(areaId) !== -1) {
            document.getElementById(areaId).setAttribute('style', 'fill: ' + originalColors[areaId]);
        }
    });
    selectedAreas = [];
    notifyChanges();
}

function addSelectedArea(selectedAreaId) {
    selectedAreas.push(selectedAreaId);
    document.getElementById(selectedAreaId).setAttribute('style', 'fill: #E65100');
    if (Boolean(isSingleSelect)) {
        lastSelectedAreaId = selectedAreaId;
    }
}

function addArea(areaId) {
    originalColors[areaId] = document.getElementById(areaId).style.color;
}

function setSelectMode(isSingleSelect) {
    this.isSingleSelect = isSingleSelect;
}

function clickOnArea(areaId) {
    if (Boolean(isSingleSelect)) {
        // single select mode
        if (Boolean(isSingleSelect) && !!lastSelectedAreaId) {
            document.getElementById(lastSelectedAreaId).setAttribute('style', 'fill: ' + originalColors[lastSelectedAreaId]);
            selectedAreas.splice(selectedAreas.indexOf(lastSelectedAreaId), 1);
            unselectArea(lastSelectedAreaId);
        }
        document.getElementById(areaId).setAttribute('style', 'fill: #E65100');
        selectedAreas.push(areaId);
        selectArea(areaId);
        lastSelectedAreaId = areaId;
    } else {
        // multiple select mode
        if (selectedAreas.indexOf(areaId) !== -1) {
            document.getElementById(areaId).setAttribute('style', 'fill: ' + originalColors[areaId]);
            selectedAreas.splice(selectedAreas.indexOf(areaId), 1);
            unselectArea(areaId);
        } else {
            document.getElementById(areaId).setAttribute('style', 'fill: #E65100');
            selectedAreas.push(areaId);
            selectArea(areaId);
        }
    }
    notifyChanges();
}