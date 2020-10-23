/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Developer API
 *
 *  Author: SmartThings
 */

import groovy.transform.EqualsAndHashCode

definition(
  name: "Smartthings Exporter API",
  namespace: "kadaan",
  author: "Joel Baranick",
  description: "API used by smartthings_exporter to pull sensor information",
  category: "My Apps",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  oauth: [displayName: "Smartthings_exporter API", displayLink: ""])

preferences {
  section() {
    paragraph "Select the sensors you want the API to have access to."
  }
  section() {
  	input "sensors", "capability.sensor", multiple: true, title: "Which sensors?", required: true
  }
}

def installed() {
  initialize()
}

def updated() {
  unsubscribe()
  initialize()
}

def initialize() {
}

mappings {
  path("/sensors") {
    action: [
      GET: "listSensors"
    ]
  }
  path("/devices") {
    action: [
      GET: "listSensors"
    ]
  }
}

def listSensors() {
    def result = []
    result << sensors.collect{deviceItem(it)}.findResults{it}
    result[0]
}

private deviceItem(device) {
    if (!device) return null
    def results = [:]
    ["id", "name", "displayName"].each {
        results << [(it) : device."$it"]
    }

    def attrsAndVals = [:]
    device.supportedAttributes?.each {
    	def value = getAttributeValue(device, it.name)
        if (value != null) attrsAndVals << [(it.name) : value]
    }
	if (attrsAndVals.size() == 0) {
    	return null
    }
    results << ["attributes" : attrsAndVals]
    results
}

private getAttributeValue(device, attribute) {
	def currentValue = device.currentValue(attribute)
	switch (attribute) {
    	case ["battery", "colorTemperature", "humidity", "hue", "illuminance", "level", "power", "saturation", "temperature", "voltage"]:
        	return valueFloat(currentValue)
    	case ["alarmState", "carbonMonoxide", "smoke", "tamper"]:
        	return valueClear(currentValue)
        case "contact":
        	return valueOneOf(currentValue, ["open", "closed"])
        case ["hvac_state", "switch"]:
        	return valueOneOf(currentValue, ["off", "on"])
        case "motion":
        	return valueOneOf(currentValue, ["inactive", "active"])
        case "presence":
        	return valueOneOf(currentValue, ["not present", "present"])
        case ["energy"]:
        	def result = valueFloat(currentValue)
            if (result == null) {
	            return result
            }
            return result * 3600000
        default:
        	return null
    }
}

private valueClear(value) {
	if (!value?.trim() || value?.trim().toString() == "clear") {
		return 0.0
	}
    return 1.0
}

private valueFloat(value) {
	if (!value) {
    	return 0.0
    }
    try {
      	float f = Float.valueOf(value).floatValue();
      	return f
    } catch (NumberFormatException nfe) {
      	return 0.0
    }
}

private valueOneOf(value, options) {
	if (!value?.trim()) {
    	return 0.0
    }
	if (value?.trim() == options[0]) {
		return 0.0
	}
	if (value?.trim() == options[1]) {
		return 1.0
	}
	return 0.0
}