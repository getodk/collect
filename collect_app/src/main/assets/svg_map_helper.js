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

var selectedAreas = new Set();
var originalColors = new Map();
var selectMode;

function onAreaClick(areaId) {
    imageMapInterface.onAreaClick(areaId);
}

function addSelectedArea(selectedAreaId) {
    selectedAreas.add(selectedAreaId);
    document.getElementById(selectedAreaId).setAttribute('style', 'fill: #E65100');
}

function addArea(areaId) {
    originalColors.set(areaId, document.getElementById(areaId).style.color);
}

function clearAreas() {
    selectedAreas.forEach(function(selectedAreaId) {
        document.getElementById(selectedAreaId).setAttribute('style', 'fill: ' + originalColors.get(selectedAreaId));
        selectedAreas.delete(selectedAreaId);
        onAreaClick(selectedAreaId);
    });
}

function setSelectMode(mode) {
    selectMode = mode;
}

function clickOnArea(areaId) {
    if (selectedAreas.has(areaId)) {
        document.getElementById(areaId).setAttribute('style', 'fill: ' + originalColors.get(areaId));
        selectedAreas.delete(areaId);
        onAreaClick(areaId);
    } else {
        if (selectMode == 'singleSelect') {
            clearAreas();
        }
        document.getElementById(areaId).setAttribute('style', 'fill: #E65100');
        selectedAreas.add(areaId);
        onAreaClick(areaId);
    }
}