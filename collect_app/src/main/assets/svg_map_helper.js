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

/*
* selectedAreas.indexOf(areaId) !== -1 : there are areas be selected （areaId）

* function indexOf(): check if specific value exist in this array and return index of that
* parameter -1 means doesn't exist

 selectedAreas.splice(selectedAreas.indexOf(lastSelectedAreaId), 1);
 means remove lastSelectedAreaId in selectedAreas
**/

function clickOnArea(areaId) {

// If I have already select this area then I click it again will remove this value from selectedAreas
//    if (selectedAreas.indexOf(areaId) !== -1) {
//        document.getElementById(areaId).setAttribute('style', 'fill: ' + originalColors[areaId]);
//        selectedAreas.splice(selectedAreas.indexOf(areaId), 1);
//        unselectArea(areaId);
//    } else {

    // !!lastSelectedAreaId means lastSelectedAreaId is not null
    // this if-else means it is a SingleSelect case
    // this case means we have already select an area and we want to click another
    // It won't help to solver this issue
        if (Boolean(isSingleSelect) && !!lastSelectedAreaId) {
            document.getElementById(lastSelectedAreaId).setAttribute('style', 'fill: ' + originalColors[lastSelectedAreaId]);
            selectedAreas.splice(selectedAreas.indexOf(lastSelectedAreaId), 1);
            unselectArea(lastSelectedAreaId);
        }
        document.getElementById(areaId).setAttribute('style', 'fill: #E65100');
        selectedAreas.push(areaId);
        selectArea(areaId);
        lastSelectedAreaId = areaId;
//    }
    notifyChanges();
}