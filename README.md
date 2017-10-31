# PointGraph

PointGraph library is a group of optimized and well-designed interactive Android Views whose main feature is a graphical representation of list of points in form of a ["spline"](https://en.wikipedia.org/wiki/Spline_(mathematics)).

[![Release](https://jitpack.io/v/User/Repo.svg)](https://jitpack.io/#johnnymartinovic/PointGraph)


## Demos
| LaGrange                    |
|:---------------------------:|
|![](assets/Demo_LaGrange.gif)|



## Setup

In your project level build.gradle add:
```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}       
```

In your app level build.gradle add:
```groovy
dependencies {
    implementation 'com.github.johnnymartinovic:PointGraph:v0.1'
}      
```

## Features
- Easily present a list of points in form of a ["splined"](https://en.wikipedia.org/wiki/Spline_(mathematics)) graph
- Use helping classes to easily make a PointsData objects needed for PointGraphs
- Allow user to select a range by moving a graph selectors
- Modify graph data and range values with included animation

## Usage
```xml
<com.johnnym.pointgraph.LaGrange
        android:id="@+id/la_grange"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

```kotlin
private val laGrange: LaGrange by bindView(R.id.la_grange)
val rangeDataList = ArrayList<Range>()
rangeDataList.add(Range(0f, 20f, 0f))
rangeDataList.add(Range(21f, 40f, 30f))
rangeDataList.add(Range(41f, 60f, 50f))
rangeDataList.add(Range(61f, 80f, 30f))
rangeDataList.add(Range(81f, 100f, 60f))

rangeData = RangeData(rangeDataList)
rangeData?.let { rangeData ->
    laGrange.setPointsData(rangeData.pointsData)
}

laGrange.addMinSelectorChangeListener(minSelectorPositionChangeListener)
laGrange.addMaxSelectorChangeListener(maxSelectorPositionChangeListener)
        
laGrange.setSelectorsValues(minValue, maxValue)
```

For more detailed view, checkout a simple [sample app](https://github.com/johnnymartinovic/PointGraph/tree/master/sample) included in this project which shows how to use the library in a more advanced way.

License
--------

    Copyright 2017 Ivan Martinovic

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.